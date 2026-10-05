package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import br.com.fiap.rei_dos_piratas.infrastructure.security.AuthCookieService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/web/auth")
public class WebAuthController {
    private final AuthController controller;
    private final AuthCookieService cookies;
    private final CsrfTokenRepository csrf;
    public WebAuthController(AuthController controller, AuthCookieService cookies, CsrfTokenRepository csrf) {
        this.controller = controller;
        this.cookies = cookies;
        this.csrf = csrf;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("token", token.getToken(), "headerName", token.getHeaderName(),
                        "parameterName", token.getParameterName()));
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest login,
                                      HttpServletRequest request, HttpServletResponse response) {
        cookies.emitir(controller.login(login), response);
        csrf.saveToken(null, request, response);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastro(@Valid @RequestBody ClienteInDto cliente,
                                         HttpServletRequest request, HttpServletResponse response) {
        cookies.emitir(controller.cadastrar(cliente), response);
        csrf.saveToken(null, request, response);
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore()).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            cookies.emitir(controller.renovar(cookies.refresh(request)), response);
            return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
        } catch (CredenciaisInvalidasException e) {
            cookies.limpar(response);
            csrf.saveToken(null, request, response);
            throw e;
        }
    }
}
