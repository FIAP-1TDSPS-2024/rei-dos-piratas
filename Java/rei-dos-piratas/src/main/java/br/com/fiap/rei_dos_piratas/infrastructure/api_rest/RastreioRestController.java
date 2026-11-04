package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.WebhookRastreioMelhorEnvio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook do Melhor Envio. Chama direto o adaptador da infraestrutura: validar a assinatura
 * e traduzir o corpo do fornecedor não é papel da camada interfaces.
 */
@Tag(name = "Rastreio", description = "Operações de recebimento de webhooks de rastreio")
@RestController
@RequestMapping("/rastreio")
public class RastreioRestController {

    private final WebhookRastreioMelhorEnvio webhook;

    public RastreioRestController(WebhookRastreioMelhorEnvio webhook) {
        this.webhook = webhook;
    }

    @Operation(summary = "Webhook de rastreio da Melhor Envio", description = "Valida a assinatura HMAC (x-me-signature) e atualiza o pedido ou a devolução do envio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook recebido (eventos sem envio identificável são ignorados)"),
            @ApiResponse(responseCode = "401", description = "Assinatura ausente ou inválida"),
            @ApiResponse(responseCode = "500", description = "Falha ao processar o evento")
    })
    @PostMapping("/webhook")
    public ResponseEntity<Void> rastreioWebhook(@RequestHeader(value = "x-me-signature", required = false) String signature,
                                                @RequestBody String rawBody) {
        this.webhook.receber(signature, rawBody);
        return ResponseEntity.ok().build();
    }
}
