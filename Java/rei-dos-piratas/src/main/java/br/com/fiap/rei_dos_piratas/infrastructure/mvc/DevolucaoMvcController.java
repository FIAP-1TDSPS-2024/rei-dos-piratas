package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Painel de devoluções do funcionário: análise, envio do retorno e conclusão (que dispara o estorno). */
@Slf4j
@Controller
@RequestMapping("/web/devolucoes")
@PreAuthorize("hasRole('PEDIDO_WRITE')")
public class DevolucaoMvcController {

    private final DevolucaoController devolucoes;
    private final PedidoController pedidos;

    public DevolucaoMvcController(DevolucaoController devolucoes, PedidoController pedidos) {
        this.devolucoes = devolucoes;
        this.pedidos = pedidos;
    }

    @GetMapping
    public String painel(@RequestParam(defaultValue = "0") int pageNumber,
                         @RequestParam(defaultValue = "20") int pageSize, Model model) {
        return listarPorStatus(StatusDevolucaoEnum.EM_ANALISE, pageNumber, pageSize, model);
    }

    @GetMapping("/status/{status}")
    public String listarPorStatus(@PathVariable StatusDevolucaoEnum status,
                                  @RequestParam(defaultValue = "0") int pageNumber,
                                  @RequestParam(defaultValue = "20") int pageSize, Model model) {
        Page<DevolucaoOutDto> pagina = this.devolucoes.findAllByStatus(pageNumber, pageSize, status);
        model.addAttribute("devolucoes", pagina.pageItems());
        model.addAttribute("statusAtual", status);
        model.addAttribute("todosStatus", StatusDevolucaoEnum.values());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", pagina.numberOfPages());
        return "devolucoes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        DevolucaoOutDto devolucao = this.devolucoes.findById(id);
        model.addAttribute("devolucao", devolucao);
        // Estorno desta devolução (o pedido pode ter outros, de cancelamento ou de outras devoluções)
        model.addAttribute("estornos", this.pedidos.listarEstornos(devolucao.pedidoId()).stream()
                .filter(estorno -> id.equals(estorno.devolucaoId()))
                .toList());
        return "devolucao-detalhes";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable Long id, RedirectAttributes redirect) {
        return executar(id, redirect, "Devolução aprovada e envio reverso criado.", () -> this.devolucoes.aprovarDevolucao(id));
    }

    @PostMapping("/{id}/recusar")
    public String recusar(@PathVariable Long id, RedirectAttributes redirect) {
        return executar(id, redirect, "Devolução recusada.", () -> this.devolucoes.recusarDevolucao(id));
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@PathVariable Long id, RedirectAttributes redirect) {
        return executar(id, redirect, "Devolução concluída e estorno solicitado ao cliente.", () -> this.devolucoes.concluirDevolucao(id));
    }

    /** Compra no Melhor Envio os fretes de retorno das devoluções selecionadas (PREPARANDO_RETORNO). */
    @PostMapping("/organizar")
    public String organizar(@RequestParam(name = "devolucoesSelecionadas", required = false) List<Long> selecionadas,
                            RedirectAttributes redirect) {
        String preparando = "redirect:/web/devolucoes/status/PREPARANDO_RETORNO";
        if (selecionadas == null || selecionadas.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Nenhuma devolução selecionada.");
            return preparando;
        }
        try {
            String mensagemErro = this.devolucoes.organizarDevolucoesParaEnvio(selecionadas);
            if (mensagemErro != null) {
                redirect.addFlashAttribute("errorMessage", mensagemErro);
                return preparando;
            }
            redirect.addFlashAttribute("successMessage", selecionadas.size() + " devolução(ões) com frete de retorno pago.");
            return "redirect:/web/devolucoes/status/AGUARDANDO_POSTAGEM_RETORNO";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
            return preparando;
        }
    }

    private static String executar(Long id, RedirectAttributes redirect, String sucesso, Runnable acao) {
        try {
            acao.run();
            redirect.addFlashAttribute("successMessage", sucesso);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
        }
        return "redirect:/web/devolucoes/" + id;
    }
}
