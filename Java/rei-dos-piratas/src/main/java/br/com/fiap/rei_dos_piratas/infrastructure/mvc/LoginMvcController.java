package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.infrastructure.security.AuthCookieService;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller MVC para a página de login web e redirect raiz.
 * A autenticacao web e processada pelo /web/auth/login (POST).
 */
@Controller
public class LoginMvcController {

    private final AuthController controller;
    private final AuthCookieService cookies;
    private final CsrfTokenRepository csrf;

    public LoginMvcController(AuthController controller, AuthCookieService cookies, CsrfTokenRepository csrf) {
        this.controller = controller;
        this.cookies = cookies;
        this.csrf = csrf;
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
     * Logout web: revoga a sessao persistida e apaga os cookies.
     * Chamado pelo formulário <form method="post" action="/web/logout"> da navbar.
     */
    @PostMapping("/web/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        controller.logout();

        // Apaga os cookies no cliente.
        cookies.limpar(response);
        csrf.saveToken(null, request, response);

        return "redirect:/web/login?logout";
    }

    @GetMapping("/web/renovar")
    public String renovar(@RequestParam(defaultValue = "/web/produtos") String destino, Model model) {
        boolean seguro = destino.startsWith("/web/") && !destino.contains("\\")
                && !destino.contains("\r") && !destino.contains("\n")
                && !destino.startsWith("/web/renovar") && !destino.startsWith("/web/auth/");
        model.addAttribute("destino", seguro ? destino : "/web/produtos");
        return "renovar";
    }
}
