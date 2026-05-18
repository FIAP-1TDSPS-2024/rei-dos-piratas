package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.ProdutoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoWebForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller MVC (Thymeleaf) para as páginas web de produto.
 * Reutiliza o ProdutoController já existente — sem duplicação de lógica.
 * Autenticação: mesmo JWT da API, lido do cookie "jwt_token" pelo JwtAuthenticationFilter.
 * Validação: delegada ao ProdutoServiceImpl via Produto domain entity — sem @Valid no form.
 */
@Slf4j
@Controller
@RequestMapping("/web/produtos")
public class ProdutoMvcController {

    private final ProdutoController controller;

    public ProdutoMvcController(ProdutoController controller) {
        this.controller = controller;
    }

    @GetMapping
    public String listAll(Model model,
                          @RequestParam(defaultValue = "0") int pageNumber,
                          @RequestParam(defaultValue = "10") int pageSize) {
        Page<ProdutoOutDto> page = controller.findAll(pageNumber, pageSize);
        if (page.pageItems().isEmpty()) {
            return "produtos-vazio";
        }
        model.addAttribute("produtos", page.pageItems());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", page.numberOfPages());
        return "produtos";
    }


    @GetMapping("/{id}")
    public String findById(Model model, @PathVariable Long id) {
        ProdutoOutDto produto = controller.findById(id);
        model.addAttribute("produto", ProdutoWebForm.fromOutDto(produto));
        return "produto-detalhes";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        if (!model.containsAttribute("produto")) {
            model.addAttribute("produto", new ProdutoWebForm());
        }
        return "produto-form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(Model model, @PathVariable Long id) {
        if (!model.containsAttribute("produto")) {
            ProdutoOutDto produto = controller.findById(id);
            model.addAttribute("produto", ProdutoWebForm.fromOutDto(produto));
        }
        return "produto-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("produto") ProdutoWebForm form,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       RedirectAttributes redirectAttrs) {

        // Funcionario vem do contexto de segurança — nunca do cliente
        form.setFuncionarioId(userDetails.getId());

        try {
            if (form.getId() == null) {
                controller.create(form.toInDto());
            } else {
                controller.update(ProdutoDtoMapper.toEntity(form.toInDto()));
            }
        } catch (ValidacaoException e) {
            // Devolve ao formulário com os erros de campo vindos da entidade de domínio
            redirectAttrs.addFlashAttribute("produto", form);
            redirectAttrs.addFlashAttribute("errosValidacao", e.getErros());
            return form.getId() == null
                    ? "redirect:/web/produtos/novo"
                    : "redirect:/web/produtos/" + form.getId() + "/editar";
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar produto", e);
            redirectAttrs.addFlashAttribute("produto", form);
            redirectAttrs.addFlashAttribute("erroGeral", "Não foi possível salvar o produto. Tente novamente.");
            return form.getId() == null
                    ? "redirect:/web/produtos/novo"
                    : "redirect:/web/produtos/" + form.getId() + "/editar";
        }

        redirectAttrs.addFlashAttribute("successMessage", "Produto salvo com sucesso!");
        return "redirect:/web/produtos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            controller.delete(id);
            redirectAttrs.addFlashAttribute("successMessage", "Produto excluído com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao excluir produto id={}", id, e);
            redirectAttrs.addFlashAttribute("errorMessage", "Não foi possível excluir o produto.");
        }
        return "redirect:/web/produtos";
    }
}
