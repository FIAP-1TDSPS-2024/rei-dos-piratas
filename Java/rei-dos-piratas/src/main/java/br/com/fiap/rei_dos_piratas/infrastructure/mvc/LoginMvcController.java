package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.infrastructure.security.TokenBlocklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

/**
 * Controller MVC para a página de login web e redirect raiz.
 * A autenticação em si é processada pelo /auth/login (POST) da API.
 */
@Controller
public class LoginMvcController {

    private final JwtUtil jwtUtil;
    private final TokenBlocklistService tokenBlocklistService;

    public LoginMvcController(JwtUtil jwtUtil, TokenBlocklistService tokenBlocklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    /** Redireciona a raiz da aplicação para a listagem web de produtos. */
    @GetMapping("/")
    public String root() {
        return "redirect:/web/produtos";
    }

    @GetMapping("/web/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Credenciais inválidas. Verifique seu e-mail e senha.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Você saiu com sucesso.");
        }
        return "login";
    }

    /**
     * Logout web: invalida o token na blocklist (server-side) e apaga o cookie.
     * Chamado pelo formulário <form method="post" action="/web/logout"> da navbar.
     */
    @PostMapping("/web/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extrai o token do cookie
        String jwt = null;
        if (request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // 2. Adiciona na blocklist
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            tokenBlocklistService.invalidar(jwt, jwtUtil.extractExpiration(jwt));
        }

        // 3. Apaga o cookie no cliente
        Cookie cookie = new Cookie("jwt_token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return "redirect:/web/login?logout";
    }
}

