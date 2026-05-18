package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaItemProdutoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CarrinhoServiceImpl implements CarrinhoService {

    private final CarrinhoRepository repository;

    private final PedidoService pedidoService;

    private final ProdutoService produtoService;

    private final ClienteService clienteService;

    public CarrinhoServiceImpl(CarrinhoRepository repository, PedidoService pedidoService, ProdutoService produtoService, ClienteService clienteService) {
        this.repository = repository;
        this.pedidoService = pedidoService;
        this.produtoService = produtoService;
        this.clienteService = clienteService;
    }

    @Transactional
    @Override
    public Carrinho adicionarProduto(ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        log.info("[CARRINHO] Cliente ID={} adicionando produto ID={} ('{}') - quantidade: {}",
                cliente.getId(), produto.getId(), produto.getNome(), itemProdutoPedido.getQuantidade());

        // Verifica o estoque
        if (produto.getEstoque() < itemProdutoPedido.getQuantidade()) {
            log.warn("[CARRINHO] Estoque insuficiente para produto ID={} ('{}') - estoque: {}, solicitado: {}",
                    produto.getId(), produto.getNome(), produto.getEstoque(), itemProdutoPedido.getQuantidade());
            throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                    produto.getNome() + " de ID " +
                    produto.getId() + " tem apenas " +
                    produto.getEstoque() + " unidades em estoque.");
        }

        Carrinho carrinho = cliente.getCarrinho();

        // 1. Busca se o produto já está no carrinho
        Optional<ItemProdutoCarrinho> itemExistente = carrinho.getProdutosAdicionados()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            // 2. Se já existe, apenas SOMA a quantidade na mesma linha
            ItemProdutoCarrinho itemAtual = itemExistente.get();
            int novaQuantidade = itemAtual.getQuantidade() + itemProdutoPedido.getQuantidade();
            log.debug("[CARRINHO] Produto ID={} já no carrinho - atualizando quantidade de {} para {}",
                    produto.getId(), itemAtual.getQuantidade(), novaQuantidade);
            itemAtual.setQuantidade(novaQuantidade);
        } else {
            // 3. Se não existe, cria o item novo
            log.debug("[CARRINHO] Produto ID={} não encontrado no carrinho - adicionando novo item", produto.getId());
            carrinho.getProdutosAdicionados().add(new ItemProdutoCarrinho(produto, itemProdutoPedido.getQuantidade()));
        }

        Carrinho carrinhoAtualizado = this.repository.update(carrinho);
        log.info("[CARRINHO] Carrinho do cliente ID={} atualizado com sucesso - total de itens: {}",
                cliente.getId(), carrinhoAtualizado.getProdutosAdicionados().size());
        return carrinhoAtualizado;
    }

    @Transactional
    @Override
    public Carrinho removerProduto(ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        log.info("[CARRINHO] Cliente ID={} removendo produto ID={} ('{}') - quantidade: {}",
                cliente.getId(), produto.getId(), produto.getNome(), itemProdutoPedido.getQuantidade());

        Carrinho carrinho = cliente.getCarrinho();

        // 1. Corrigido o filtro: agora ele olha para o 'item' da lista
        Optional<ItemProdutoCarrinho> produtoRemovido = carrinho
                .getProdutosAdicionados()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (produtoRemovido.isEmpty()) {
            log.warn("[CARRINHO] Tentativa de remover produto ID={} que não está no carrinho do cliente ID={}",
                    produto.getId(), cliente.getId());
            throw new RegraDeNegocioException("Esse produto não foi incluído no carrinho.");
        }

        ItemProdutoCarrinho itemAtual = produtoRemovido.get();

        // 2. Calculamos a nova quantidade
        int novaQuantidade = itemAtual.getQuantidade() - itemProdutoPedido.getQuantidade();

        // 3. Corrigida a lógica matemática: se zerar ou ficar negativo, remove da lista
        if (novaQuantidade <= 0) {
            log.debug("[CARRINHO] Quantidade resultante <= 0 - removendo produto ID={} completamente do carrinho", produto.getId());
            carrinho.getProdutosAdicionados().remove(itemAtual);
        } else {
            log.debug("[CARRINHO] Atualizando quantidade do produto ID={} de {} para {}",
                    produto.getId(), itemAtual.getQuantidade(), novaQuantidade);
            itemAtual.setQuantidade(novaQuantidade);
        }

        Carrinho carrinhoAtualizado = this.repository.update(carrinho);
        log.info("[CARRINHO] Produto removido com sucesso - carrinho do cliente ID={} com {} item(ns) restante(s)",
                cliente.getId(), carrinhoAtualizado.getProdutosAdicionados().size());
        return carrinhoAtualizado;
    }

    @Transactional
    @Override
    public Carrinho limparCarrinho() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        log.info("[CARRINHO] Limpando carrinho do cliente ID={}", cliente.getId());

        Carrinho carrinho = cliente.getCarrinho();
        carrinho.setProdutosAdicionados(new ArrayList<>());

        Carrinho carrinhoLimpo = this.repository.update(carrinho);
        log.info("[CARRINHO] Carrinho do cliente ID={} limpo com sucesso", cliente.getId());
        return carrinhoLimpo;
    }

    @Override
    public Carrinho visualizarCarrinho() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.debug("[CARRINHO] Visualizando carrinho do cliente ID={}", userDetails.getId());
        return this.clienteService.findById(userDetails.getId()).getCarrinho();
    }

    @Transactional
    @Override
    public Pedido finalizarCompra(Endereco enderecoEntrega, Long freteServiceId) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        log.info("[CARRINHO] Cliente ID={} iniciando finalização de compra - CEP entrega: {}, serviço frete ID: {}",
                cliente.getId(), enderecoEntrega.getCep(), freteServiceId);

        Carrinho carrinho = cliente.getCarrinho();

        if (carrinho.getProdutosAdicionados().isEmpty()) {
            log.warn("[CARRINHO] Cliente ID={} tentou finalizar compra com carrinho vazio", cliente.getId());
            throw new RegraDeNegocioException("O carrinho está vazio! Adicione itens para finalizar a compra!");
        }

        log.debug("[CARRINHO] Carrinho do cliente ID={} possui {} item(ns) - convertendo para pedido",
                cliente.getId(), carrinho.getProdutosAdicionados().size());

        List<ItemProdutoPedido> produtosAdicionados = carrinho
                .getProdutosAdicionados()
                .stream()
                .map(JpaItemProdutoMapper::toPedido)
                .collect(Collectors.toList());

        Pedido pedido = new Pedido(cliente, enderecoEntrega, produtosAdicionados, freteServiceId);
        Pedido pedidoFinalizado = this.pedidoService.fazerPedido(pedido);

        log.info("[CARRINHO] Compra finalizada com sucesso - pedido ID={} criado para cliente ID={}",
                pedidoFinalizado.getId(), cliente.getId());

        limparCarrinho();

        return pedidoFinalizado;
    }
}
