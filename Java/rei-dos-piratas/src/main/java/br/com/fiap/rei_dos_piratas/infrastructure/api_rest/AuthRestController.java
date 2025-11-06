package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthController controller;

    public AuthRestController(AuthController controller) {
        this.controller = controller;
    }


    @Operation(summary = "Método para login, recebe username e senha")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = controller.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Método para cadastro de clientes, recebe informações do cliente")
    @PostMapping("/cadastro")
    public ResponseEntity<ClienteOutDto> cadastro(@Valid @RequestBody ClienteInDto clienteInDto) {
        ClienteOutDto cliente = this.controller.cadastrar(clienteInDto);
        return ResponseEntity.ok(cliente);
    }
}
