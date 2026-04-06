package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller MVC para a página de login web e redirect raiz.
 * A autenticação em si é processada pelo Spring Security via /web/login (POST).
 */
@Controller
public class LoginMvcController {

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
}

