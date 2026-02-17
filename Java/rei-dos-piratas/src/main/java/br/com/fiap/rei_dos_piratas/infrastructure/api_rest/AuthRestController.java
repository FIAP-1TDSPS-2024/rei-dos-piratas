package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints de login e cadastro de clientes")
@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthController controller;

    public AuthRestController(AuthController controller) {
        this.controller = controller;
    }

    @Operation(summary = "Login", description = "Recebe username e senha e retorna token/infos de autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = controller.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cadastro de cliente", description = "Cria um novo cliente a partir dos dados informados")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponse> cadastro(@Valid @RequestBody ClienteInDto clienteInDto) {
        AuthResponse authResponse = this.controller.cadastrar(clienteInDto);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(authResponse);
    }
}
