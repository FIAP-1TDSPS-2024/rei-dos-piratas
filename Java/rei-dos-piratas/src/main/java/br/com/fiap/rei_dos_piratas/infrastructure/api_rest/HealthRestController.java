package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "Endpoint de verificação da aplicação")
@RestController
@RequestMapping("/health")
public class HealthRestController {

    @Operation(summary = "Health check", description = "Verifica se a aplicação está saudável")
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
