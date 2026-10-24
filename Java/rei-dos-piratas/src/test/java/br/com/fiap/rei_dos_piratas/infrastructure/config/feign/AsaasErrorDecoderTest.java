package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.ClienteCobrancaRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PagamentoCobrancaRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TokenizacaoCartaoRequestDto;
import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AsaasErrorDecoderTest {

    private static final String RECUSA_ASAAS =
            "{\"errors\":[{\"code\":\"invalid_creditCard\",\"description\":\"Transação não autorizada. Verifique os dados do cartão.\"}]}";

    private static final String RECUSA_ASAAS_CLIENTE =
            "{\"errors\":[{\"code\":\"invalid_creditCard\",\"description\":\"Erro ao criar cliente.\"}]}";


    private final AsaasErrorDecoder decoder = new AsaasErrorDecoder();

    private String chaveDoMetodo(String nome, Class<?> parametro) throws NoSuchMethodException {
        return Feign.configKey(CobrancaAppClient.class, CobrancaAppClient.class.getMethod(nome, parametro));
    }

    private Response resposta(int status, String corpo) {
        Request request = Request.create(Request.HttpMethod.POST, "/v3/payments", Map.of(), null, StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .reason("erro")
                .request(request)
                .headers(Map.of())
                .body(corpo, StandardCharsets.UTF_8)
                .build();
    }

    @Test
    void erro400AoCriarCobrancaDeveVirarPagamentoRecusadoComDescricaoDoAsaas() throws Exception {
        Exception erro = decoder.decode(chaveDoMetodo("criarNovaCobranca", PagamentoCobrancaRequestDto.class), resposta(400, RECUSA_ASAAS));

        assertThat(erro)
                .isInstanceOf(PagamentoRecusadoException.class)
                .hasMessageContaining("Transação não autorizada");
    }

    @Test
    void erro400AoTokenizarCartaoDeveVirarPagamentoRecusado() throws Exception {
        Exception erro = decoder.decode(chaveDoMetodo("tokenizarCartao", TokenizacaoCartaoRequestDto.class), resposta(400, RECUSA_ASAAS));

        assertThat(erro)
                .isInstanceOf(PagamentoRecusadoException.class)
                .hasMessageContaining("Transação não autorizada");
    }

    @Test
    void erro400AoCriarClienteContinuaComoErroDeApiExterna() throws Exception {
        Exception erro = decoder.decode(chaveDoMetodo("criarNovoCliente", ClienteCobrancaRequestDto.class), resposta(400, RECUSA_ASAAS_CLIENTE));

        assertThat(erro)
                .isInstanceOf(ApiExternaException.class)
                .isNotInstanceOf(PagamentoRecusadoException.class);
    }

    @Test
    void erro500AoCriarCobrancaContinuaComoErroDeApiExterna() throws Exception {
        Exception erro = decoder.decode(chaveDoMetodo("criarNovaCobranca", PagamentoCobrancaRequestDto.class), resposta(503, "indisponível"));

        assertThat(erro)
                .isInstanceOf(ApiExternaException.class)
                .hasMessageContaining("indisponível");
    }
}
