package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.web.IpDoComprador;
import br.com.fiap.rei_dos_piratas.interfaces.controller.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Área do cliente no painel web: carrinho, checkout e pagamento do pedido.
 * Só delega aos controllers de {@code interfaces}; nenhuma regra de negócio aqui.
 * Dados do cartão só passam para a tokenização: nunca vão para log, modelo ou flash.
 */
@Slf4j
@Controller
@RequestMapping("/web/conta")
@PreAuthorize("hasRole('ENDERECO_MANAGE')")
public class ContaClienteMvcController {

    private static final String CARRINHO = "redirect:/web/conta/carrinho";

    private final CarrinhoController carrinho;
    private final EnderecoController enderecos;
    private final CartaoClienteController cartoes;
    private final FreteController frete;
    private final PedidoController pedidos;
    private final DevolucaoController devolucoes;

    public ContaClienteMvcController(CarrinhoController carrinho, EnderecoController enderecos,
                                     CartaoClienteController cartoes, FreteController frete, PedidoController pedidos,
                                     DevolucaoController devolucoes) {
        this.devolucoes = devolucoes;
        this.carrinho = carrinho;
        this.enderecos = enderecos;
        this.cartoes = cartoes;
        this.frete = frete;
        this.pedidos = pedidos;
    }

    // ─── CARRINHO ────────────────────────────────────────────────────────────

    @GetMapping("/carrinho")
    public String carrinho(Model model) {
        CarrinhoOutDto atual = this.carrinho.visualizarCarrinho();
        model.addAttribute("carrinho", atual);
        model.addAttribute("total", total(atual));
        return "conta/carrinho";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes redirect) {
        return executar(redirect, CARRINHO, "Produto adicionado ao carrinho.",
                () -> this.carrinho.adicionarProduto(new ItemProdutoInDto(produtoId, quantidade)));
    }

    @PostMapping("/carrinho/remover")
    public String remover(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                          RedirectAttributes redirect) {
        return executar(redirect, CARRINHO, "Produto removido do carrinho.",
                () -> this.carrinho.removerProduto(new ItemProdutoInDto(produtoId, quantidade)));
    }

    // ─── CHECKOUT ────────────────────────────────────────────────────────────

    @GetMapping("/checkout")
    public String checkout(@RequestParam(required = false) Long enderecoId, Model model, RedirectAttributes redirect) {
        CarrinhoOutDto atual = this.carrinho.visualizarCarrinho();
        if (atual.produtosAdicionados() == null || atual.produtosAdicionados().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "O carrinho está vazio! Adicione itens para finalizar a compra.");
            return CARRINHO;
        }
        model.addAttribute("carrinho", atual);
        model.addAttribute("total", total(atual));
        model.addAttribute("enderecos", this.enderecos.findAll(0, 50).pageItems());
        model.addAttribute("tiposPagamento", TipoPagamentoEnum.values());

        if (enderecoId != null) {
            model.addAttribute("enderecoId", enderecoId);
            model.addAttribute("cartoes", this.cartoes.listarCartoesDoClienteAtual());
            try {
                EnderecoOutDto endereco = this.enderecos.findById(enderecoId);
                model.addAttribute("opcoesFrete", this.frete.calcularFreteProdutos(
                        new ConsultaFreteDto(endereco.cep(), itensDoCarrinho(atual))));
            } catch (RuntimeException e) {
                log.warn("[WEB-CONTA] Falha ao cotar o frete do checkout: {}", e.getMessage());
                model.addAttribute("errorMessage", MensagemWeb.de(e));
            }
        }
        return "conta/checkout";
    }

    @PostMapping("/enderecos")
    public String novoEndereco(@ModelAttribute EnderecoInDto endereco, RedirectAttributes redirect) {
        try {
            EnderecoOutDto salvo = this.enderecos.save(endereco);
            return "redirect:/web/conta/checkout?enderecoId=" + salvo.id();
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
            return "redirect:/web/conta/checkout";
        }
    }

    /**
     * Cria o pedido pelos itens do carrinho e o esvazia. Usa a rota direta de pedido (aceita cartão),
     * não a finalização do carrinho, que ainda não recebe dados de cartão.
     */
    @PostMapping("/checkout")
    public String finalizar(@RequestParam Long enderecoId, @RequestParam Long freteServiceId,
                            @RequestParam TipoPagamentoEnum tipoPagamento,
                            @RequestParam(required = false) Long cartaoId,
                            @RequestParam(required = false) String titular,
                            @RequestParam(required = false) String numero,
                            @RequestParam(required = false) String mesValidade,
                            @RequestParam(required = false) String anoValidade,
                            @RequestParam(required = false) String cvv,
                            @RequestParam(defaultValue = "1") Integer parcelas,
                            HttpServletRequest request, RedirectAttributes redirect) {
        CartaoPedidoInDto cartao = null;
        if (tipoPagamento == TipoPagamentoEnum.CREDIT_CARD) {
            cartao = cartaoId != null
                    ? new CartaoPedidoInDto(cartaoId, null, null, null, null, null, parcelas)
                    : new CartaoPedidoInDto(null, titular, numero, mesValidade, anoValidade, cvv, parcelas);
        }
        PedidoInDto pedido = new PedidoInDto(freteServiceId, enderecoId,
                itensDoCarrinho(this.carrinho.visualizarCarrinho()), tipoPagamento, cartao);

        try {
            PedidoOutDto criado = this.pedidos.fazerPedido(pedido, IpDoComprador.de(request));
            this.carrinho.limparCarrinho();
            log.info("[WEB-CONTA] Pedido ID={} criado pelo checkout web ({})", criado.id(), tipoPagamento);
            return "redirect:/web/conta/pedidos/" + criado.id() + "/pagamento";
        } catch (RuntimeException e) {
            // A mensagem vem das exceções de domínio; o cartão não entra no flash nem no log
            log.warn("[WEB-CONTA] Checkout recusado ({}): {}", tipoPagamento, e.getClass().getSimpleName());
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
            return "redirect:/web/conta/checkout?enderecoId=" + enderecoId;
        }
    }

    // ─── PAGAMENTO ───────────────────────────────────────────────────────────

    @GetMapping("/pedidos/{id}/pagamento")
    public String pagamento(@PathVariable Long id, Model model) {
        PedidoOutDto pedido = this.pedidos.findById(id);
        model.addAttribute("pedido", pedido);

        boolean aguardando = pedido.status() == StatusEnum.AGUARDANDO_PAGAMENTO;
        try {
            if (aguardando && pedido.tipoPagamento() == TipoPagamentoEnum.PIX) {
                model.addAttribute("qrCode", this.pedidos.obterQrCodeCobrancaPix(id));
            } else if (aguardando && pedido.tipoPagamento() == TipoPagamentoEnum.BOLETO) {
                model.addAttribute("boleto", this.pedidos.obterBoleto(id));
            }
        } catch (RuntimeException e) {
            log.warn("[WEB-CONTA] Dados de pagamento do pedido ID={} indisponíveis: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Não foi possível carregar os dados do pagamento agora: " + MensagemWeb.de(e));
        }
        return "conta/pagamento";
    }

    // ─── MEUS PEDIDOS ────────────────────────────────────────────────────────

    @GetMapping("/pedidos")
    public String meusPedidos(@RequestParam(defaultValue = "0") int pageNumber,
                              @RequestParam(defaultValue = "10") int pageSize, Model model) {
        var pagina = this.pedidos.findAllByCliente(pageNumber, pageSize);
        model.addAttribute("pedidos", pagina.pageItems());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", pagina.numberOfPages());
        return "conta/pedidos";
    }

    /** findById valida que o pedido é do cliente logado; as devoluções só são buscadas depois disso. */
    @GetMapping("/pedidos/{id}")
    public String detalhePedido(@PathVariable Long id, Model model) {
        PedidoOutDto pedido = this.pedidos.findById(id);
        List<DevolucaoOutDto> devolucoesDoPedido = this.devolucoes.findAllByPedidoId(id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("estornos", this.pedidos.listarEstornos(id));
        model.addAttribute("devolucoes", devolucoesDoPedido);
        // Só decide quais botões aparecem; as regras continuam validadas no serviço
        model.addAttribute("podeCancelar", pedido.status() == StatusEnum.AGUARDANDO_PAGAMENTO
                || pedido.status() == StatusEnum.PREPARANDO_ENVIO);
        model.addAttribute("podeDevolver", pedido.status() == StatusEnum.ENTREGUE && devolucoesDoPedido.stream()
                .allMatch(d -> d.status() == StatusDevolucaoEnum.CANCELADO || d.status() == StatusDevolucaoEnum.RETORNADO));
        return "conta/pedido-detalhes";
    }

    @PostMapping("/pedidos/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        return executar(redirect, "redirect:/web/conta/pedidos/" + id,
                "Pedido cancelado. Se já estava pago, o estorno foi solicitado.", () -> this.pedidos.cancelarPedido(id));
    }

    @GetMapping("/pedidos/{id}/devolucao")
    public String formularioDevolucao(@PathVariable Long id, Model model) {
        model.addAttribute("pedido", this.pedidos.findById(id));
        model.addAttribute("motivos", this.devolucoes.listarMotivos());
        return "conta/devolucao-form";
    }

    /** {@code itemPedidoId} e {@code quantidade} vêm na mesma ordem; itens com quantidade 0 ficam de fora. */
    @PostMapping("/pedidos/{id}/devolucao")
    public String solicitarDevolucao(@PathVariable Long id, @RequestParam Long motivoId,
                                     @RequestParam(required = false) String descricao,
                                     @RequestParam List<Long> itemPedidoId, @RequestParam List<Integer> quantidade,
                                     RedirectAttributes redirect) {
        List<ItemDevolucaoInDto> itens = new java.util.ArrayList<>();
        for (int i = 0; i < itemPedidoId.size() && i < quantidade.size(); i++) {
            if (quantidade.get(i) != null && quantidade.get(i) > 0) {
                itens.add(new ItemDevolucaoInDto(itemPedidoId.get(i), quantidade.get(i)));
            }
        }
        String formulario = "redirect:/web/conta/pedidos/" + id + "/devolucao";
        if (itens.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Informe a quantidade de ao menos um item para devolver.");
            return formulario;
        }
        try {
            this.devolucoes.solicitarDevolucao(new DevolucaoInDto(id, motivoId, descricao, itens));
            redirect.addFlashAttribute("successMessage", "Devolução solicitada. Acompanhe o andamento por aqui.");
            return "redirect:/web/conta/pedidos/" + id;
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
            return formulario;
        }
    }

    // ─── AUXILIARES ──────────────────────────────────────────────────────────

    private static List<ItemProdutoInDto> itensDoCarrinho(CarrinhoOutDto carrinho) {
        return carrinho.produtosAdicionados().stream()
                .map(item -> new ItemProdutoInDto(item.produto().id(), item.quantidade()))
                .toList();
    }

    private static BigDecimal total(CarrinhoOutDto carrinho) {
        if (carrinho.produtosAdicionados() == null) {
            return BigDecimal.ZERO;
        }
        return carrinho.produtosAdicionados().stream()
                .map(ItemProdutoOutDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Executa a ação; exceções de domínio viram mensagem na tela em vez de página de erro. */
    private static String executar(RedirectAttributes redirect, String destino, String sucesso, Runnable acao) {
        try {
            acao.run();
            redirect.addFlashAttribute("successMessage", sucesso);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", MensagemWeb.de(e));
        }
        return destino;
    }
}
