package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.RastreioController;
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

@Tag(name = "Rastreio", description = "Operações de recebimento de webhooks de rastreio")
@RestController
@RequestMapping("/rastreio")
public class RastreioRestController {

    private final RastreioController controller;

    public RastreioRestController(RastreioController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Webhook de rastreio da Melhor Envio", description = "Recebe requisições de rastreio e encaminha para o domínio interno correspondente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook recebido com sucesso")
    })
    @PostMapping("/webhook")
    public ResponseEntity<Void> rastreioWebhook(@RequestHeader("x-me-signature") String signature,
                                                @RequestBody String rawBody) {
        this.controller.rastreioWebhook(signature, rawBody);
        return ResponseEntity.ok().build();
    }
}

