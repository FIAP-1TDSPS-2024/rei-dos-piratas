package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * Controller MVC (Thymeleaf) para o painel de pedidos dos funcionários.
 * Reutiliza o PedidoController da camada de interface — sem duplicação de lógica.
 * Autenticação: JWT lido do cookie "jwt_token" pelo JwtAuthenticationFilter.
 * Acesso: restrito a funcionários com a role PEDIDO_WRITE.
 */
@Slf4j
@Controller
@RequestMapping("/web/pedidos")
@PreAuthorize("hasRole('PEDIDO_WRITE')")
public class PedidoMvcController {

    private final PedidoController controller;

    public PedidoMvcController(PedidoController controller) {
        this.controller = controller;
    }

    // ─── LISTAGENS POR STATUS ────────────────────────────────────────────────

    /**
     * Painel principal: lista pedidos aguardando envio (PREPARANDO_ENVIO).
     */
    @GetMapping
    public String painelPedidos(Model model,
                                @RequestParam(defaultValue = "0") int pageNumber,
                                @RequestParam(defaultValue = "10") int pageSize) {
        return listarPorStatus(StatusEnum.PREPARANDO_ENVIO, pageNumber, pageSize, model);
    }

    /**
     * Lista pedidos filtrados por um status específico.
     */
    @GetMapping("/status/{status}")
    public String listarPorStatus(@PathVariable StatusEnum status,
                                  @RequestParam(defaultValue = "0") int pageNumber,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  Model model) {
        Page<PedidoOutDto> page = controller.findAllByStatus(pageNumber, pageSize, status);
        model.addAttribute("pedidos", page.pageItems());
        model.addAttribute("statusAtual", status);
        model.addAttribute("todosStatus", StatusEnum.values());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", page.numberOfPages());
        return "pedidos";
    }

    // ─── DETALHES ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String detalhesPedido(Model model, @PathVariable Long id) {
        PedidoOutDto pedido = controller.findById(id);
        model.addAttribute("pedido", pedido);
        return "pedido-detalhes";
    }

    // ─── ORGANIZAR PEDIDOS PARA ENVIO ────────────────────────────────────────

    /**
     * Exibe o formulário de seleção de pedidos para organização (PREPARANDO_ENVIO).
     */
    @GetMapping("/organizar")
    public String organizarForm(Model model,
                                @RequestParam(defaultValue = "0") int pageNumber,
                                @RequestParam(defaultValue = "50") int pageSize) {
        Page<PedidoOutDto> page = controller.findAllByStatus(pageNumber, pageSize, StatusEnum.PREPARANDO_ENVIO);
        model.addAttribute("pedidos", page.pageItems());
        model.addAttribute("statusAtual", StatusEnum.PREPARANDO_ENVIO);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", page.numberOfPages());
        return "pedidos-organizar";
    }

    /**
     * Processa a organização dos pedidos selecionados pelo funcionário.
     */
    @PostMapping("/organizar")
    public String organizarPedidos(@RequestParam(name = "pedidosSelecionados", required = false) List<Long> pedidosSelecionados,
                                   RedirectAttributes redirectAttrs) {
        if (pedidosSelecionados == null || pedidosSelecionados.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Nenhum pedido selecionado para organização.");
            return "redirect:/web/pedidos/organizar";
        }

        try {
            String mensagemErro = controller.organizarPedidosParaEnvio(pedidosSelecionados);
            if (mensagemErro != null) {
                redirectAttrs.addFlashAttribute("errorMessage", mensagemErro);
                return "redirect:/web/pedidos/organizar";
            }
            redirectAttrs.addFlashAttribute("successMessage", pedidosSelecionados.size() + " pedido(s) organizados com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/web/pedidos/organizar";
        } catch (Exception e) {
            log.error("Erro ao organizar pedidos", e);
            redirectAttrs.addFlashAttribute("errorMessage", "Erro inesperado ao organizar pedidos. Tente novamente.");
            return "redirect:/web/pedidos/organizar";
        }

        return "redirect:/web/pedidos/status/AGUARDANDO_GERACAO_ETIQUETA";
    }

    // ─── GERAR ETIQUETAS ─────────────────────────────────────────────────────

    /**
     * Exibe formulário de seleção para geração de etiquetas (AGUARDANDO_GERACAO_ETIQUETA).
     */
    @GetMapping("/etiquetas")
    public String etiquetasForm(Model model,
                                @RequestParam(defaultValue = "0") int pageNumber,
                                @RequestParam(defaultValue = "50") int pageSize) {
        Page<PedidoOutDto> page = controller.findAllByStatus(pageNumber, pageSize, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        model.addAttribute("pedidos", page.pageItems());
        model.addAttribute("statusAtual", StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", page.numberOfPages());
        return "pedidos-etiquetas";
    }

    /**
     * Processa a geração de etiquetas dos pedidos selecionados.
     */
    @PostMapping("/etiquetas/gerar")
    public String gerarEtiquetas(@RequestParam(name = "pedidosSelecionados", required = false) List<Long> pedidosSelecionados,
                                 RedirectAttributes redirectAttrs) {
        if (pedidosSelecionados == null || pedidosSelecionados.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Nenhum pedido selecionado para geração de etiquetas.");
            return "redirect:/web/pedidos/etiquetas";
        }

        try {
            var resultado = controller.gerarEtiquetasParaEnvio(pedidosSelecionados);
            long sucessos = resultado.values().stream().filter(v -> v.startsWith("Etiqueta gerada")).count();
            long falhas = resultado.size() - sucessos;

            if (falhas > 0) {
                redirectAttrs.addFlashAttribute("warningMessage",
                        sucessos + " etiqueta(s) geradas com sucesso, " + falhas + " com falha.");
            } else {
                redirectAttrs.addFlashAttribute("successMessage",
                        sucessos + " etiqueta(s) geradas com sucesso!");
            }
            redirectAttrs.addFlashAttribute("resultadoEtiquetas", resultado);
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/web/pedidos/etiquetas";
        } catch (Exception e) {
            log.error("Erro ao gerar etiquetas", e);
            redirectAttrs.addFlashAttribute("errorMessage", "Erro inesperado ao gerar etiquetas. Tente novamente.");
            return "redirect:/web/pedidos/etiquetas";
        }

        return "redirect:/web/pedidos/status/AGUARDANDO_POSTAGEM";
    }

    // ─── IMPRIMIR ETIQUETAS ───────────────────────────────────────────────────

    /**
     * Exibe formulário de seleção para impressão de etiquetas (AGUARDANDO_POSTAGEM).
     */
    @GetMapping("/imprimir")
    public String imprimirForm(Model model,
                               @RequestParam(defaultValue = "0") int pageNumber,
                               @RequestParam(defaultValue = "50") int pageSize) {
        Page<PedidoOutDto> page = controller.findAllByStatus(pageNumber, pageSize, StatusEnum.AGUARDANDO_POSTAGEM);
        model.addAttribute("pedidos", page.pageItems());
        model.addAttribute("statusAtual", StatusEnum.AGUARDANDO_POSTAGEM);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", page.numberOfPages());
        return "pedidos-imprimir";
    }

    /**
     * Processa a impressão das etiquetas dos pedidos selecionados e redireciona para a URL de impressão.
     */
    @PostMapping("/imprimir")
    public String imprimirEtiquetas(@RequestParam(name = "pedidosSelecionados", required = false) List<Long> pedidosSelecionados,
                                    RedirectAttributes redirectAttrs) {
        if (pedidosSelecionados == null || pedidosSelecionados.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Nenhum pedido selecionado para impressão.");
            return "redirect:/web/pedidos/imprimir";
        }

        try {
            String urlImpressao = controller.imprimirEtiquetasEnvio(pedidosSelecionados);
            redirectAttrs.addFlashAttribute("successMessage", "Etiquetas prontas para impressão!");
            redirectAttrs.addFlashAttribute("urlImpressao", urlImpressao);
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/web/pedidos/imprimir";
        } catch (Exception e) {
            log.error("Erro ao imprimir etiquetas", e);
            redirectAttrs.addFlashAttribute("errorMessage", "Erro inesperado ao gerar link de impressão. Tente novamente.");
            return "redirect:/web/pedidos/imprimir";
        }

        return "redirect:/web/pedidos/imprimir";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            controller.cancelarPedido(id);
            redirectAttrs.addFlashAttribute("successMessage", "Pedido #" + id + " cancelado.");
        } catch (WrongStatusException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao cancelar pedido id={}", id, e);
            redirectAttrs.addFlashAttribute("errorMessage", "Erro ao cancelar o pedido. Tente novamente.");
        }
        return "redirect:/web/pedidos";
    }
}

