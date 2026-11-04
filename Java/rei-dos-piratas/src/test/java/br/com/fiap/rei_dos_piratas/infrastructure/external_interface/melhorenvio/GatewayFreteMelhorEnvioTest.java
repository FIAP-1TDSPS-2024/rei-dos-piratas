package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.entity.Volume;
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Contrato do adaptador com a API do Melhor Envio: Feign real (com o Jackson do projeto e o interceptor
 * do token) contra um WireMock.
 */
@SpringBootTest
class GatewayFreteMelhorEnvioTest {

    @RegisterExtension
    static WireMockExtension melhorEnvio = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void apontarParaOWireMock(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.frete-app.url", melhorEnvio::baseUrl);
        // Timeout curto só no teste, para simular o Melhor Envio sem responder
        registry.add("spring.cloud.openfeign.client.config.frete-app.read-timeout", () -> "1500");
    }

    @Autowired GatewayFrete gateway;
    @MockBean ProvedorTokenMelhorEnvio provedorToken;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void tokenValido() {
        when(provedorToken.tokenValido()).thenReturn(new Token("tok_me", "ref_me", 2_592_000));
    }

    private List<ItemProdutoPedido> itens() {
        Produto produto = new Produto();
        produto.setId(7L);
        produto.setLargura(new BigDecimal("13.5"));
        produto.setAltura(new BigDecimal("2.0"));
        produto.setProfundidade(new BigDecimal("19.0"));
        produto.setPeso(new BigDecimal("0.2"));
        produto.setPreco(new BigDecimal("45.90"));
        return List.of(new ItemProdutoPedido(produto, 2));
    }

    private JsonNode corpoEnviado() throws Exception {
        return json.readTree(melhorEnvio.getAllServeEvents().getFirst().getRequest().getBodyAsString());
    }

    private void cotacaoRetorna(String corpo) {
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/calculate")).willReturn(okJson(corpo)));
    }

    // ---- Cotação ----

    @Test
    void cotar_DeveEnviarOrigemDestinoEProdutos() throws Exception {
        // Arrange
        cotacaoRetorna("[]");

        // Act
        gateway.cotar("01310100", "01001000", itens());

        // Assert
        JsonNode corpo = corpoEnviado();
        assertThat(corpo.at("/from/postal_code").asText()).isEqualTo("01310100");
        assertThat(corpo.at("/to/postal_code").asText()).isEqualTo("01001000");
        JsonNode produto = corpo.at("/products/0");
        assertThat(produto.get("id").asLong()).isEqualTo(7L);
        assertThat(produto.get("width").decimalValue()).isEqualByComparingTo("13.5");
        assertThat(produto.get("height").decimalValue()).isEqualByComparingTo("2.0");
        assertThat(produto.get("length").decimalValue()).isEqualByComparingTo("19.0");
        assertThat(produto.get("weight").decimalValue()).isEqualByComparingTo("0.2");
        assertThat(produto.get("insurance_value").decimalValue()).isEqualByComparingTo("45.90");
        assertThat(produto.get("quantity").asInt()).isEqualTo(2);
    }

    @Test
    void cotar_DeveEnviarOTokenDoMelhorEnvio() {
        // Arrange
        cotacaoRetorna("[]");

        // Act
        gateway.cotar("01310100", "01001000", itens());

        // Assert
        melhorEnvio.verify(postRequestedFor(urlPathEqualTo("/api/v2/me/shipment/calculate"))
                .withHeader("Authorization", equalTo("Bearer tok_me")));
    }

    @Test
    void cotar_DeveMapearAsOpcoesRetornadas() {
        // Arrange
        cotacaoRetorna("""
                [{"id":2,"name":"SEDEX","price":"32.50","custom_price":"30.00","discount":"2.50","currency":"R$",
                  "delivery_time":3,"company":{"id":1,"name":"Correios","picture":"https://example.com/correios.png"}}]""");

        // Act
        List<OpcaoFrete> opcoes = gateway.cotar("01310100", "01001000", itens());

        // Assert
        assertThat(opcoes).hasSize(1);
        OpcaoFrete sedex = opcoes.getFirst();
        assertThat(sedex.id()).isEqualTo(2L);
        assertThat(sedex.nome()).isEqualTo("SEDEX");
        assertThat(sedex.preco()).isEqualByComparingTo("32.50");
        assertThat(sedex.precoPersonalizado()).isEqualByComparingTo("30.00");
        assertThat(sedex.desconto()).isEqualByComparingTo("2.50");
        assertThat(sedex.moeda()).isEqualTo("R$");
        assertThat(sedex.prazoDias()).isEqualTo(3);
        assertThat(sedex.transportadora().id()).isEqualTo(1L);
        assertThat(sedex.transportadora().nome()).isEqualTo("Correios");
        assertThat(sedex.transportadora().logo()).isEqualTo("https://example.com/correios.png");
    }

    @Test
    void cotar_ErroDoMelhorEnvioDeveVirarApiExternaException() {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/calculate"))
                .willReturn(aResponse().withStatus(422).withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"The given data was invalid.\"}")));

        // Act & Assert
        assertThatThrownBy(() -> gateway.cotar("01310100", "01001000", itens()))
                .isInstanceOf(ApiExternaException.class);
    }

    @Test
    void cotar_SemRespostaDoMelhorEnvioDeveVirarApiExternaException() {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/calculate"))
                .willReturn(okJson("[]").withFixedDelay(3000)));

        // Act & Assert
        assertThatThrownBy(() -> gateway.cotar("01310100", "01001000", itens()))
                .isInstanceOf(ApiExternaException.class);
    }

    // ---- Envio do pedido ----

    private SolicitacaoEnvio solicitacaoEnvio() {
        ParteEnvio loja = new ParteEnvio("Rei dos Piratas", "contato@reidospiratas.com.br", "1133334444", null,
                "12345678901234", "00000000000191", "4721100", "Avenida Brasil", null, "123", "Centro", "São Paulo", "12345678", "SP");
        ParteEnvio cliente = new ParteEnvio("João Silva", "joao@gmail.com", "11992131234", "52998224725",
                null, null, null, "Praça da Sé", null, "45", "Sé", "São Paulo", "01001000", "SP");
        return new SolicitacaoEnvio(3L, loja, cliente,
                List.of(new ProdutoEnvio("One Piece Vol. 1", 2, new BigDecimal("45.90"))),
                new Volume(7, 16, 26, new BigDecimal("0.4")),
                "Rei dos Piratas", "Entrega do pedido 10", new BigDecimal("121.80"));
    }

    @Test
    void criarEnvio_DeveEnviarServicoPartesProdutosVolumeEOpcoes() throws Exception {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/cart")).willReturn(okJson("""
                {"id":"123e4567-e89b-12d3-a456-426614174010","protocol":"ORD-1","price":"21.19","delivery_max":6}""")));

        // Act
        gateway.criarEnvio(solicitacaoEnvio());

        // Assert
        JsonNode corpo = corpoEnviado();
        assertThat(corpo.get("service").asLong()).isEqualTo(3L);
        assertThat(corpo.at("/from/name").asText()).isEqualTo("Rei dos Piratas");
        assertThat(corpo.at("/from/company_document").asText()).isEqualTo("12345678901234");
        assertThat(corpo.at("/from/state_register").asText()).isEqualTo("00000000000191");
        assertThat(corpo.at("/from/economic_activity_code").asText()).isEqualTo("4721100");
        assertThat(corpo.at("/from/postal_code").asText()).isEqualTo("12345678");
        assertThat(corpo.at("/from/state_abbr").asText()).isEqualTo("SP");
        assertThat(corpo.at("/to/document").asText()).isEqualTo("52998224725");
        assertThat(corpo.at("/to/address").asText()).isEqualTo("Praça da Sé");
        assertThat(corpo.at("/to/number").asText()).isEqualTo("45");
        assertThat(corpo.at("/to/district").asText()).isEqualTo("Sé");
        assertThat(corpo.at("/to/city").asText()).isEqualTo("São Paulo");
        assertThat(corpo.at("/products/0/name").asText()).isEqualTo("One Piece Vol. 1");
        assertThat(corpo.at("/products/0/quantity").asText()).isEqualTo("2");
        assertThat(corpo.at("/products/0/unitary_value").asText()).isEqualTo("45.90");
        assertThat(corpo.at("/volumes/0/height").asInt()).isEqualTo(7);
        assertThat(corpo.at("/volumes/0/width").asInt()).isEqualTo(16);
        assertThat(corpo.at("/volumes/0/length").asInt()).isEqualTo(26);
        assertThat(corpo.at("/volumes/0/weight").decimalValue()).isEqualByComparingTo("0.4");
        assertThat(corpo.at("/options/platform").asText()).isEqualTo("Rei dos Piratas");
        assertThat(corpo.at("/options/reminder").asText()).isEqualTo("Entrega do pedido 10");
        assertThat(corpo.at("/options/insurance_value").decimalValue()).isEqualByComparingTo("121.80");
        assertThat(corpo.at("/options/receipt").asBoolean()).isFalse();
        assertThat(corpo.at("/options/own_hand").asBoolean()).isFalse();
        assertThat(corpo.at("/options/reverse").asBoolean()).isFalse();
        assertThat(corpo.at("/options/non_commercial").asBoolean()).isFalse();
    }

    @Test
    void criarEnvio_DeveMapearOEnvioCriado() {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/cart")).willReturn(okJson("""
                {"id":"123e4567-e89b-12d3-a456-426614174010","protocol":"ORD-1","price":"21.19","delivery_max":6}""")));

        // Act
        EnvioCriado envio = gateway.criarEnvio(solicitacaoEnvio());

        // Assert
        assertThat(envio.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174010"));
        assertThat(envio.protocolo()).isEqualTo("ORD-1");
        assertThat(envio.preco()).isEqualByComparingTo("21.19");
        assertThat(envio.prazoMaximoDias()).isEqualTo(6);
    }

    @Test
    void criarEnvioReverso_DeveEnviarOEnvioOriginalContatosEPacote() throws Exception {
        // Arrange
        UUID original = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/cart/reverse")).willReturn(okJson("""
                {"id":"123e4567-e89b-12d3-a456-426614174011","protocol":"ORD-2","price":"15.00"}""")));

        // Act
        EnvioCriado envio = gateway.criarEnvioReverso(new SolicitacaoEnvioReverso(7L, "joao@gmail.com", "11992131234",
                new BigDecimal("91.80"), original, new Volume(5, 16, 26, new BigDecimal("0.2"))));

        // Assert
        JsonNode corpo = corpoEnviado();
        assertThat(corpo.get("service").asInt()).isEqualTo(7);
        assertThat(corpo.get("new_sender_mail").asText()).isEqualTo("joao@gmail.com");
        assertThat(corpo.get("new_sender_phone").asText()).isEqualTo("11992131234");
        assertThat(corpo.get("insurance_value").decimalValue()).isEqualByComparingTo("91.80");
        assertThat(corpo.get("order_id").asText()).isEqualTo(original.toString());
        assertThat(corpo.at("/package/height").asInt()).isEqualTo(5);
        assertThat(corpo.at("/package/weight").decimalValue()).isEqualByComparingTo("0.2");
        assertThat(corpo.at("/options/own_hand").asBoolean()).isFalse();
        assertThat(corpo.at("/options/receipt").asBoolean()).isFalse();
        assertThat(envio.id()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174011"));
        assertThat(envio.protocolo()).isEqualTo("ORD-2");
        assertThat(envio.preco()).isEqualByComparingTo("15.00");
    }

    @Test
    void comprarEnvios_DeveEnviarOsIdsERetornarSucesso() throws Exception {
        // Arrange
        UUID envio = UUID.randomUUID();
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/checkout")).willReturn(okJson("""
                {"purchase":{"id":"p1","status":"paid"}}""")));

        // Act
        ResultadoCompraEnvios resultado = gateway.comprarEnvios(List.of(envio));

        // Assert
        assertThat(resultado.sucesso()).isTrue();
        assertThat(corpoEnviado().get(0).asText()).isEqualTo(envio.toString());
    }

    @Test
    void comprarEnvios_RecusaDeveVirMensagemPriorizandoAMessage() {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/checkout")).willReturn(okJson("""
                {"message":"Saldo insuficiente","error":"insufficient_balance"}""")));

        // Act & Assert
        assertThat(gateway.comprarEnvios(List.of(UUID.randomUUID())).mensagemErro()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void comprarEnvios_RecusaSoComErrorDeveUsarOError() {
        // Arrange
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/checkout")).willReturn(okJson("""
                {"error":"Carrinho vazio"}""")));

        // Act & Assert
        assertThat(gateway.comprarEnvios(List.of(UUID.randomUUID())).mensagemErro()).isEqualTo("Carrinho vazio");
    }

    @Test
    void gerarEtiquetas_DeveRetornarOResultadoPorEnvio() throws Exception {
        // Arrange
        UUID ok = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");
        UUID falhou = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/generate")).willReturn(okJson("""
                {"123e4567-e89b-12d3-a456-426614174010":{"status":true,"message":"OK"},
                 "123e4567-e89b-12d3-a456-426614174011":{"status":false,"message":"Sem saldo"},
                 "generate_key":"chave"}""")));

        // Act
        Map<UUID, ResultadoEtiqueta> resultado = gateway.gerarEtiquetas(List.of(ok, falhou));

        // Assert
        assertThat(corpoEnviado().at("/orders/0").asText()).isEqualTo(ok.toString());
        assertThat(resultado).containsOnlyKeys(ok, falhou);
        assertThat(resultado.get(ok)).isEqualTo(new ResultadoEtiqueta(true, "OK"));
        assertThat(resultado.get(falhou)).isEqualTo(new ResultadoEtiqueta(false, "Sem saldo"));
    }

    @Test
    void obterLinkImpressao_DeveRetornarAUrl() throws Exception {
        // Arrange
        UUID envio = UUID.randomUUID();
        melhorEnvio.stubFor(post(urlPathEqualTo("/api/v2/me/shipment/print")).willReturn(okJson("""
                {"url":"https://example.com/etiquetas.pdf"}""")));

        // Act
        String url = gateway.obterLinkImpressao(List.of(envio));

        // Assert
        assertThat(url).isEqualTo("https://example.com/etiquetas.pdf");
        assertThat(corpoEnviado().at("/orders/0").asText()).isEqualTo(envio.toString());
    }
}
