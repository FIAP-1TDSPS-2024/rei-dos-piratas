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
 * Decodificador de erros customizado para a API Melhor Envio.
 * Traduz respostas HTTP de erro em {@link ApiExternaException} com mensagens descritivas,
 * permitindo que a aplicação trate falhas externas sem expor detalhes técnicos ao usuário.
 *
 * Mapeamento de status:
 *  - 400: Requisição inválida (dados enviados incorretamente)
 *  - 401: Token de acesso inválido ou expirado
 *  - 403: Sem permissão para executar a operação
 *  - 422: Dados inválidos ou regra de negócio violada na API
 *  - 429: Limite de requisições atingido (rate limit)
 *  - 5xx: Serviço do Melhor Envio indisponível
 */
@Slf4j
public class MelhorEnvioErrorDecoder implements ErrorDecoder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String bodyMessage = extractBodyMessage(response);

        log.error("[FRETE] Erro na chamada '{}' — HTTP {}: {}", methodKey, status, bodyMessage);

        return switch (status) {
            case 400 -> new ApiExternaException(
                    "Requisição inválida para a API de frete: " + bodyMessage);
            case 401 -> new ApiExternaException(
                    "Token de acesso ao Melhor Envio inválido ou expirado. Verifique o token configurado.");
            case 403 -> new ApiExternaException(
                    "Sem permissão para realizar esta operação na API de frete.");
            case 404 -> new ApiExternaException(
                    "Recurso não encontrado na API de frete: " + bodyMessage);
            case 422 -> new ApiExternaException(
                    "Dados inválidos rejeitados pela API de frete: " + bodyMessage);
            case 429 -> new ApiExternaException(
                    "Limite de requisições da API de frete atingido. Tente novamente em instantes.");
            default -> {
                if (status >= 500) {
                    yield new ApiExternaException(
                            "Serviço do Melhor Envio indisponível no momento (HTTP " + status + "). Tente mais tarde.");
                }
                yield new ApiExternaException(
                        "Erro inesperado na API de frete (HTTP " + status + "): " + bodyMessage);
            }
        };
    }

    /**
     * Tenta extrair a mensagem de erro do corpo da resposta JSON.
     * A API Melhor Envio retorna erros no formato {"message": "...", "errors": {...}}.
     * Caso o parsing falhe, retorna o corpo bruto como texto.
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
                JsonNode root = OBJECT_MAPPER.readTree(rawBody);

                // Tenta campo "message" primeiro
                if (root.has("message") && !root.get("message").isNull()) {
                    String message = root.get("message").asText();
                    // Agrega erros específicos de campos, se existirem
                    if (root.has("errors") && root.get("errors").isObject()) {
                        StringBuilder sb = new StringBuilder(message).append(" — ");
                        root.get("errors").fields().forEachRemaining(entry ->
                                sb.append(entry.getKey()).append(": ")
                                  .append(entry.getValue().toString()).append("; "));
                        return sb.toString().trim();
                    }
                    return message;
                }

                // Fallback: retorna o JSON completo limitado
                return rawBody.length() > 300 ? rawBody.substring(0, 300) + "..." : rawBody;

            } catch (Exception jsonEx) {
                // Não é JSON — retorna o texto bruto
                return rawBody.length() > 300 ? rawBody.substring(0, 300) + "..." : rawBody;
            }

        } catch (IOException e) {
            log.warn("[FRETE] Não foi possível ler o corpo da resposta de erro.", e);
            return "não foi possível ler o corpo da resposta";
        }
    }
}

