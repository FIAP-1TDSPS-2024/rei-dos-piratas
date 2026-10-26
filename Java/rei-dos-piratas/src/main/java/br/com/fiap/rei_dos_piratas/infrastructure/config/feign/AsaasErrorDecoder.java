package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodificador de erros customizado para a API de cobrança Asaas.
 * Traduz respostas HTTP de erro em {@link ApiExternaException} com mensagens descritivas.
 *
 * Mapeamento de status:
 *  - 400: Requisição inválida (dados enviados incorretamente)
 *  - 401: API key inválida ou ausente
 *  - 403: Sem permissão para executar a operação
 *  - 404: Recurso não encontrado
 *  - 429: Limite de requisições atingido (rate limit)
 *  - 5xx: Serviço de cobrança indisponível
 */
@Slf4j
public class AsaasErrorDecoder implements ErrorDecoder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Código do Asaas para cartão não autorizado (a descrição é genérica e pode mudar). */
    private static final String CODIGO_CARTAO_RECUSADO = "invalid_creditCard";

    /** Corpo de erro do Asaas: descrições para a mensagem e códigos para log e decisão. */
    private record CorpoErro(String mensagem, List<String> codigos) {}

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        CorpoErro corpo = extrairCorpo(response);
        String bodyMessage = corpo.mensagem();

        // Só código e descrição do Asaas: o corpo da requisição (com dados do cartão) nunca é logado
        log.error("[COBRANCA] Erro na chamada '{}' — HTTP {}: código(s)={} descrição={}",
                methodKey, status, corpo.codigos(), bodyMessage);

        return switch (status) {
            case 400 -> {
                if (chamadaDeCartao(methodKey) && corpo.codigos().contains(CODIGO_CARTAO_RECUSADO)) {
                    yield new PagamentoRecusadoException("Pagamento recusado: " + bodyMessage);
                }
                yield new ApiExternaException("Requisição inválida para a API de cobrança: " + bodyMessage);
            }
            case 401 -> new ApiExternaException(
                    "API key da API de cobrança inválida ou ausente. Verifique a chave configurada.");
            case 403 -> new ApiExternaException(
                    "Sem permissão para realizar esta operação na API de cobrança.");
            case 404 -> new ApiExternaException(
                    "Recurso não encontrado na API de cobrança: " + bodyMessage);
            case 429 -> new ApiExternaException(
                    "Limite de requisições da API de cobrança atingido. Tente novamente em instantes.");
            default -> {
                if (status >= 500) {
                    yield new ApiExternaException(
                            "Serviço de cobrança indisponível no momento (HTTP " + status + "). Tente mais tarde.");
                }
                yield new ApiExternaException(
                        "Erro inesperado na API de cobrança (HTTP " + status + "): " + bodyMessage);
            }
        };
    }

    /** Recusa de cartão só faz sentido na tokenização e na criação da cobrança. */
    private boolean chamadaDeCartao(String methodKey) {
        return methodKey.contains("#criarNovaCobranca(") || methodKey.contains("#tokenizarCartao(");
    }

    /**
     * Extrai descrições e códigos do corpo JSON.
     * A API Asaas retorna erros no formato {"errors": [{"code": "...", "description": "..."}]}.
     * Caso o parsing falhe, a mensagem é o corpo bruto limitado e não há códigos.
     */
    private CorpoErro extrairCorpo(Response response) {
        if (response.body() == null) {
            return new CorpoErro("sem corpo na resposta", List.of());
        }

        try (InputStream bodyStream = response.body().asInputStream()) {
            byte[] bodyBytes = bodyStream.readAllBytes();
            if (bodyBytes.length == 0) {
                return new CorpoErro("sem corpo na resposta", List.of());
            }

            String rawBody = new String(bodyBytes, StandardCharsets.UTF_8);

            try {
                JsonNode errors = OBJECT_MAPPER.readTree(rawBody).get("errors");
                if (errors != null && errors.isArray() && !errors.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    List<String> codigos = new ArrayList<>();
                    errors.forEach(error -> {
                        if (error.hasNonNull("code")) {
                            codigos.add(error.get("code").asText());
                        }
                        String description = error.hasNonNull("description")
                                ? error.get("description").asText()
                                : error.toString();
                        sb.append(description).append("; ");
                    });
                    return new CorpoErro(sb.toString().trim(), codigos);
                }
            } catch (Exception jsonEx) {
                // Não é JSON — retorna o texto bruto
            }

            String mensagem = rawBody.length() > 300 ? rawBody.substring(0, 300) + "..." : rawBody;
            return new CorpoErro(mensagem, List.of());

        } catch (IOException e) {
            log.warn("[COBRANCA] Não foi possível ler o corpo da resposta de erro.", e);
            return new CorpoErro("não foi possível ler o corpo da resposta", List.of());
        }
    }
}
