package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Login, cadastro, renovação e logout da sessão atual")
@RestController
@RequestMapping("/auth")
public class AuthRestController {
    private final AuthController controller;
    public AuthRestController(AuthController controller) { this.controller = controller; }

    @Operation(summary = "Login", description = "Cria uma sessao e retorna access token e refresh token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(controller.login(request));
    }

    @Operation(summary = "Cadastro de cliente", description = "Cria o cliente e sua sessao inicial")
    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponse> cadastro(@Valid @RequestBody ClienteInDto request) {
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore()).body(controller.cadastrar(request));
    }

    @Operation(summary = "Renovar autenticacao", description = "Rotaciona o refresh token sem estender o prazo da sessao")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(controller.renovar(request.refreshToken()));
    }

    @Operation(summary = "Logout", description = "Revoga somente a sessao autenticada")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        controller.logout();
        return ResponseEntity.ok().build();
    }
}
