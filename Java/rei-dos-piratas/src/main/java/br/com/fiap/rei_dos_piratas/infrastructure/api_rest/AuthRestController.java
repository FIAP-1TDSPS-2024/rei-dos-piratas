package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.LoginRequest;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.infrastructure.security.TokenBlocklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Tag(name = "Autenticação", description = "Endpoints de login e cadastro de clientes")
@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthController controller;
    private final JwtUtil jwtUtil;
    private final TokenBlocklistService tokenBlocklistService;

    public AuthRestController(AuthController controller, JwtUtil jwtUtil, TokenBlocklistService tokenBlocklistService) {
        this.controller = controller;
        this.jwtUtil = jwtUtil;
        this.tokenBlocklistService = tokenBlocklistService;
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
    public ResponseEntity<AuthResponse> cadastro(@RequestBody ClienteInDto clienteInDto) {
        AuthResponse authResponse = this.controller.cadastrar(clienteInDto);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Logout", description = "Invalida o token JWT atual e apaga o cookie de sessão web")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extrai o token (header ou cookie)
        String jwt = extrairToken(request);

        // 2. Adiciona na blocklist com a data de expiração original
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            tokenBlocklistService.invalidar(jwt, jwtUtil.extractExpiration(jwt));
        }

        // 3. Apaga o cookie jwt_token no cliente (logout web)
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true em produção com HTTPS
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    private String extrairToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "jwt_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
