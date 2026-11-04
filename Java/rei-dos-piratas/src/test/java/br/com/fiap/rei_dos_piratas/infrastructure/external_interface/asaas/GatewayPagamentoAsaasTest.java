package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrato do adaptador com a API do Asaas: sobe o Feign real (com a configuração de Jackson do projeto)
 * contra um WireMock e verifica o JSON enviado e a tradução das respostas e erros.
 */
@SpringBootTest
class GatewayPagamentoAsaasTest {

    @RegisterExtension
    static WireMockExtension asaas = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void apontarParaOWireMock(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.cobranca-app.url", asaas::baseUrl);
    }

    @Autowired GatewayPagamento gateway;
    private final ObjectMapper json = new ObjectMapper();

    private Cliente cliente() {
        Cliente cliente = new Cliente(1L, "joaosilva", "João Silva", "joao@gmail.com", "SenhaSegura123", true,
                LocalDate.now(), new Perfil(1L, "CLIENT", "Perfil de cliente", null), LocalDate.of(1990, 5, 15),
                SexoEnum.M, "52998224725", "11992131234", new Carrinho(1L, new ArrayList<>()));
        cliente.setIdCobranca("cus_000001");
        return cliente;
    }

    private Endereco endereco() {
        return new Endereco(1L, 123, "01001000", "Avenida Paulista", "Bela Vista", true,
                new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", null);
    }

    private JsonNode corpoEnviado() throws Exception {
        return json.readTree(asaas.getAllServeEvents().getFirst().getRequest().getBodyAsString());
    }

    private static boolean ausente(JsonNode campo) {
        return campo.isMissingNode() || campo.isNull();
    }

    private SolicitacaoCobranca solicitacao(TipoPagamentoEnum tipo, Integer parcelas, String token, String ip, Integer diasCancelamento) {
        return new SolicitacaoCobranca("cus_000001", tipo, new BigDecimal("221.19"), LocalDate.of(2026, 11, 4),
                "cobrança do pedido 10", "10", parcelas, token, ip, diasCancelamento);
    }

    private void cobrancaCriadaComStatus(String status) {
        asaas.stubFor(post(urlPathEqualTo("/v3/payments/")).willReturn(okJson("""
                {"id":"pay_000001","netValue":219.20,"status":"%s",
                 "bankSlipUrl":"https://sandbox.asaas.com/b/pdf/pay_000001"}""".formatted(status))));
    }

    // ---- Isolamento da configuração Feign ----

    @Test
    @org.junit.jupiter.api.DisplayName("Chamadas ao Asaas não dependem nem enviam o token OAuth do Melhor Envio")
    void chamadaAoAsaasNaoEnviaOTokenDoMelhorEnvio() {
        asaas.stubFor(post(urlPathEqualTo("/v3/customers")).willReturn(okJson("{\"id\":\"cus_000009\"}")));

        gateway.registrarCliente(cliente(), endereco());

        asaas.verify(postRequestedFor(urlPathEqualTo("/v3/customers")).withoutHeader("Authorization"));
    }

    // ---- Cliente ----

    @Test
    void registrarClienteEnviaOsDadosDoClienteEDevolveOIdExterno() throws Exception {
        asaas.stubFor(post(urlPathEqualTo("/v3/customers")).willReturn(okJson("{\"id\":\"cus_000009\"}")));

        String id = gateway.registrarCliente(cliente(), endereco());

        assertThat(id).isEqualTo("cus_000009");
        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("name").asText()).isEqualTo("João Silva");
        assertThat(corpo.path("cpfCnpj").asText()).isEqualTo("52998224725");
        assertThat(corpo.path("email").asText()).isEqualTo("joao@gmail.com");
        assertThat(corpo.path("mobilePhone").asText()).isEqualTo("11992131234");
        assertThat(corpo.path("postalCode").asText()).isEqualTo("01001000");
        assertThat(corpo.path("addressNumber").asText()).isEqualTo("123");
        assertThat(corpo.path("externalReference").asText()).isEqualTo("1");
    }

    // ---- Cartão ----

    @Test
    void tokenizarCartaoEnviaCartaoTitularEIpEDevolveOToken() throws Exception {
        asaas.stubFor(post(urlPathEqualTo("/v3/creditCard/tokenizeCreditCard")).willReturn(okJson(
                "{\"creditCardNumber\":\"1234\",\"creditCardBrand\":\"VISA\",\"creditCardToken\":\"tok_abc123\"}")));

        CartaoTokenizado cartao = gateway.tokenizarCartao(cliente(), endereco(),
                new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987"), "200.1.2.3");

        assertThat(cartao.token()).isEqualTo("tok_abc123");
        assertThat(cartao.ultimosDigitos()).isEqualTo("1234");
        assertThat(cartao.bandeira()).isEqualTo("VISA");
        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("customer").asText()).isEqualTo("cus_000001");
        assertThat(corpo.path("remoteIp").asText()).isEqualTo("200.1.2.3");
        assertThat(corpo.path("creditCard").path("holderName").asText()).isEqualTo("JOAO SILVA");
        assertThat(corpo.path("creditCard").path("number").asText()).isEqualTo("4111111111111234");
        assertThat(corpo.path("creditCard").path("expiryMonth").asText()).isEqualTo("12");
        assertThat(corpo.path("creditCard").path("expiryYear").asText()).isEqualTo("2030");
        assertThat(corpo.path("creditCard").path("ccv").asText()).isEqualTo("987");
        assertThat(corpo.path("creditCardHolderInfo").path("name").asText()).isEqualTo("João Silva");
        assertThat(corpo.path("creditCardHolderInfo").path("cpfCnpj").asText()).isEqualTo("52998224725");
        assertThat(corpo.path("creditCardHolderInfo").path("postalCode").asText()).isEqualTo("01001000");
        assertThat(corpo.path("creditCardHolderInfo").path("addressNumber").asText()).isEqualTo("123");
    }

    @Test
    void tokenizacaoRecusadaViraPagamentoRecusado() {
        asaas.stubFor(post(urlPathEqualTo("/v3/creditCard/tokenizeCreditCard")).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"code\":\"invalid_creditCard\",\"description\":\"Transação não autorizada.\"}]}")));

        assertThatThrownBy(() -> gateway.tokenizarCartao(cliente(), endereco(),
                new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987"), "200.1.2.3"))
                .isInstanceOf(PagamentoRecusadoException.class);
    }

    // ---- Cobrança ----

    @Test
    void cobrancaPixEnviaValorVencimentoEReferenciaSemCamposDeCartao() throws Exception {
        cobrancaCriadaComStatus("PENDING");

        gateway.criarCobranca(solicitacao(TipoPagamentoEnum.PIX, 1, null, null, null));

        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("customer").asText()).isEqualTo("cus_000001");
        assertThat(corpo.path("billingType").asText()).isEqualTo("PIX");
        assertThat(corpo.path("value").decimalValue()).isEqualByComparingTo("221.19");
        assertThat(corpo.path("dueDate").asText()).isEqualTo("2026-11-04");
        assertThat(corpo.path("description").asText()).isEqualTo("cobrança do pedido 10");
        assertThat(corpo.path("externalReference").asText()).isEqualTo("10");
        assertThat(ausente(corpo.path("installmentCount"))).isTrue();
        assertThat(ausente(corpo.path("creditCardToken"))).isTrue();
        assertThat(ausente(corpo.path("remoteIp"))).isTrue();
    }

    @Test
    void cobrancaCartaoAVistaEnviaValorTokenEIp() throws Exception {
        cobrancaCriadaComStatus("CONFIRMED");

        gateway.criarCobranca(solicitacao(TipoPagamentoEnum.CREDIT_CARD, 1, "tok_abc123", "200.1.2.3", null));

        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("billingType").asText()).isEqualTo("CREDIT_CARD");
        assertThat(corpo.path("value").decimalValue()).isEqualByComparingTo("221.19");
        assertThat(corpo.path("creditCardToken").asText()).isEqualTo("tok_abc123");
        assertThat(corpo.path("remoteIp").asText()).isEqualTo("200.1.2.3");
        assertThat(ausente(corpo.path("installmentCount"))).isTrue();
        assertThat(ausente(corpo.path("totalValue"))).isTrue();
    }

    @Test
    void cobrancaCartaoParceladaEnviaParcelasEValorTotalSemValue() throws Exception {
        cobrancaCriadaComStatus("CONFIRMED");

        gateway.criarCobranca(solicitacao(TipoPagamentoEnum.CREDIT_CARD, 3, "tok_abc123", "200.1.2.3", null));

        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("installmentCount").asInt()).isEqualTo(3);
        assertThat(corpo.path("totalValue").decimalValue()).isEqualByComparingTo("221.19");
        assertThat(ausente(corpo.path("value"))).isTrue();
    }

    @Test
    void cobrancaBoletoEnviaDiasParaCancelarORegistro() throws Exception {
        cobrancaCriadaComStatus("PENDING");

        gateway.criarCobranca(solicitacao(TipoPagamentoEnum.BOLETO, 1, null, null, 1));

        JsonNode corpo = corpoEnviado();
        assertThat(corpo.path("billingType").asText()).isEqualTo("BOLETO");
        assertThat(corpo.path("value").decimalValue()).isEqualByComparingTo("221.19");
        assertThat(corpo.path("daysAfterDueDateToRegistrationCancellation").asInt()).isEqualTo(1);
        assertThat(ausente(corpo.path("remoteIp"))).isTrue();
    }

    @Test
    void respostaDaCobrancaViraCobrancaCriadaComIdValorLiquidoEUrlDoBoleto() {
        cobrancaCriadaComStatus("PENDING");

        CobrancaCriada criada = gateway.criarCobranca(solicitacao(TipoPagamentoEnum.BOLETO, 1, null, null, 1));

        assertThat(criada.idCobranca()).isEqualTo("pay_000001");
        assertThat(criada.valorLiquido()).isEqualByComparingTo("219.20");
        assertThat(criada.urlBoleto()).isEqualTo("https://sandbox.asaas.com/b/pdf/pay_000001");
        assertThat(criada.confirmada()).isFalse();
    }

    @Test
    void cartaoAprovadoNaCriacaoVemComoConfirmada() {
        cobrancaCriadaComStatus("CONFIRMED");

        CobrancaCriada criada = gateway.criarCobranca(solicitacao(TipoPagamentoEnum.CREDIT_CARD, 1, "tok_abc123", "200.1.2.3", null));

        assertThat(criada.confirmada()).isTrue();
    }

    @Test
    void cartaoEmAnaliseDeRiscoNaoVemComoConfirmada() {
        cobrancaCriadaComStatus("AWAITING_RISK_ANALYSIS");

        CobrancaCriada criada = gateway.criarCobranca(solicitacao(TipoPagamentoEnum.CREDIT_CARD, 1, "tok_abc123", "200.1.2.3", null));

        assertThat(criada.confirmada()).isFalse();
    }

    @Test
    void cartaoRecusadoNaCobrancaViraPagamentoRecusado() {
        asaas.stubFor(post(urlPathEqualTo("/v3/payments/")).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"code\":\"invalid_creditCard\",\"description\":\"Transação não autorizada.\"}]}")));

        assertThatThrownBy(() -> gateway.criarCobranca(solicitacao(TipoPagamentoEnum.CREDIT_CARD, 1, "tok_abc123", "200.1.2.3", null)))
                .isInstanceOf(PagamentoRecusadoException.class);
    }

    @Test
    void indisponibilidadeDoAsaasViraErroDeApiExterna() {
        asaas.stubFor(post(urlPathEqualTo("/v3/payments/")).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> gateway.criarCobranca(solicitacao(TipoPagamentoEnum.PIX, 1, null, null, null)))
                .isInstanceOf(ApiExternaException.class);
    }

    // ---- PIX e boleto ----

    @Test
    void qrCodePixDevolveImagemCopiaEColaEExpiracao() {
        asaas.stubFor(get(urlPathEqualTo("/v3/payments/pay_000001/pixQrCode")).willReturn(okJson("""
                {"success":true,"encodedImage":"iVBORw0KGgo=","payload":"00020126580014br.gov.bcb.pix",
                 "expirationDate":"2026-11-04 10:30:00"}""")));

        QrCodePix qrCode = gateway.obterQrCodePix("pay_000001");

        assertThat(qrCode.sucesso()).isTrue();
        assertThat(qrCode.imagemBase64()).isEqualTo("iVBORw0KGgo=");
        assertThat(qrCode.copiaECola()).isEqualTo("00020126580014br.gov.bcb.pix");
        assertThat(qrCode.expiraEm()).isEqualTo(LocalDateTime.of(2026, 11, 4, 10, 30, 0));
    }

    @Test
    void linhaDigitavelDevolveLinhaECodigoDeBarras() {
        asaas.stubFor(get(urlPathEqualTo("/v3/payments/pay_000001/identificationField")).willReturn(okJson("""
                {"identificationField":"00190000090123456789012345678901234567890123","nossoNumero":"6543",
                 "barCode":"00191234500000221190000001234567890123456789"}""")));

        LinhaDigitavel linha = gateway.obterLinhaDigitavel("pay_000001");

        assertThat(linha.linhaDigitavel()).isEqualTo("00190000090123456789012345678901234567890123");
        assertThat(linha.codigoBarras()).isEqualTo("00191234500000221190000001234567890123456789");
    }
}
