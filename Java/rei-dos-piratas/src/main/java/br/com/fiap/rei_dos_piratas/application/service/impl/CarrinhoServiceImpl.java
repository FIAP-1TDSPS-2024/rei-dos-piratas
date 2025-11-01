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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Carrinho adicionarProduto(Long clienteId, ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());
        Cliente cliente = clienteService.findById(clienteId);

        //Verifica o produto tem estoque suficiente para adicionar a quantidade desejada
        //Quando for criado o pedido, essa quandidade vai ser revisada, caso o estoque tenha sido atualizado
        if (produto.getEstoque() < itemProdutoPedido.getQuantidade()) {
            throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                    produto.getNome() + " de ID " +
                    produto.getId() + " tem apenas " +
                    produto.getEstoque() + " unidades em estoque.");
        }

        Carrinho carrinho = cliente.getCarrinho();

        // Cria uma lista mutável a partir da lista atual
        List<ItemProdutoCarrinho> items = new ArrayList<>(carrinho.getProdutosAdicionados());

        // Adiciona o novo item
        items.add(new ItemProdutoCarrinho(produto, itemProdutoPedido.getQuantidade()));

        // Atualiza o carrinho
        carrinho.setProdutosAdicionados(items);

        return this.repository.update(carrinho);
    }

    @Transactional
    @Override
    public Carrinho removerProduto(Long clienteId, ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());
        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();
        List<ItemProdutoCarrinho> items = new ArrayList<>(carrinho.getProdutosAdicionados());

        Optional<ItemProdutoCarrinho> produtoRemovido = carrinho
                .getProdutosAdicionados()
                .stream()
                .filter(item -> itemProdutoPedido.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (produtoRemovido.isEmpty()) {
            throw new RegraDeNegocioException("Esse produto não foi incluído no carrinho.");
        }

        if (produtoRemovido.get().getQuantidade() < itemProdutoPedido.getQuantidade()) {
            items.remove(produtoRemovido.get());
            carrinho.setProdutosAdicionados(items);
        }
        else {
            produtoRemovido.get().setQuantidade(
                    produtoRemovido.get().getQuantidade() - itemProdutoPedido.getQuantidade());
        }

        return this.repository.update(carrinho);
    }

    @Transactional
    @Override
    public Carrinho limparCarrinho(Long clienteId) {
        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();

        List<ItemProdutoCarrinho> items = new ArrayList<>();

        carrinho.setProdutosAdicionados(items);

        return this.repository.update(carrinho);
    }

    @Override
    public Carrinho visualizarCarrinho(Long clienteId) {
        return this.clienteService.findById(clienteId).getCarrinho();
    }

    @Transactional
    @Override
    public Pedido finalizarCompra(Long clienteId) {

        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();

        if (carrinho.getProdutosAdicionados().isEmpty()) {
            throw new RegraDeNegocioException("O carrinho está vazio! Adicione itens para finalizar a compra!");
        }

        List<ItemProdutoPedido> produtosAdicionados = carrinho
                                                        .getProdutosAdicionados()
                                                        .stream()
                                                        .map(JpaItemProdutoMapper::toPedido)
                                                        .collect(Collectors.toList());
        Pedido pedido = new Pedido(cliente, produtosAdicionados);

        Pedido pedidoFinalizado = this.pedidoService.fazerPedido(pedido);

        //Se a operação de criação de pedido for um sucesso, o carrinho é limpo
        limparCarrinho(clienteId);

        return pedidoFinalizado;
    }
}
