package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String bodyMessage = extractBodyMessage(response);

        log.error("[COBRANCA] Erro na chamada '{}' — HTTP {}: {}", methodKey, status, bodyMessage);

        return switch (status) {
            case 400 -> new ApiExternaException(
                    "Requisição inválida para a API de cobrança: " + bodyMessage);
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

    /**
     * Extrai a mensagem de erro do corpo JSON.
     * A API Asaas retorna erros no formato {"errors": [{"code": "...", "description": "..."}]}.
     * Caso o parsing falhe, retorna o corpo bruto limitado.
     */
    private String extractBodyMessage(Response response) {
        if (response.body() == null) {
            return "sem corpo na resposta";
        }

        try (InputStream bodyStream = response.body().asInputStream()) {
            byte[] bodyBytes = bodyStream.readAllBytes();
            if (bodyBytes.length == 0) {
                return "sem corpo na resposta";
            }

            String rawBody = new String(bodyBytes, StandardCharsets.UTF_8);

            try {
                JsonNode errors = OBJECT_MAPPER.readTree(rawBody).get("errors");
                if (errors != null && errors.isArray() && !errors.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    errors.forEach(error -> {
                        String description = error.hasNonNull("description")
                                ? error.get("description").asText()
                                : error.toString();
                        sb.append(description).append("; ");
                    });
                    return sb.toString().trim();
                }
            } catch (Exception jsonEx) {
                // Não é JSON — retorna o texto bruto
            }

            return rawBody.length() > 300 ? rawBody.substring(0, 300) + "..." : rawBody;

        } catch (IOException e) {
            log.warn("[COBRANCA] Não foi possível ler o corpo da resposta de erro.", e);
            return "não foi possível ler o corpo da resposta";
        }
    }
}
