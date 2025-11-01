package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Carrinho adicionarProduto(Long clienteId, Long produtoId, int quantidade) {
        Produto produto = produtoService.findById(produtoId);
        Cliente cliente = clienteService.findById(clienteId);

        //Verifica o produto tem estoque suficiente para adicionar a quantidade desejada
        //Quando for criado o pedido, essa quandidade vai ser revisada, caso o estoque tenha sido atualizado
        if (produto.getEstoque() < quantidade) {
            throw new EstoqueInsuficienteException("Estoque insuficiente! O produto " +
                    produto.getNome() + " de ID " +
                    produto.getId() + " tem apenas " +
                    produto.getEstoque() + " unidades em estoque.");
        }

        Carrinho carrinho = cliente.getCarrinho();

        //O produto é adicionado ao carrinho
        carrinho.getProdutosAdicionados().add(
                new ItemProduto(produto, quantidade));

        return this.repository.update(carrinho);
    }

    @Override
    public Carrinho removerProduto(Long clienteId, Long produtoId, int quantidade) {
        Produto produto = produtoService.findById(produtoId);
        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();

        Optional<ItemProduto> produtoRemovido = carrinho
                .getProdutosAdicionados()
                .stream()
                .filter(itemProduto -> itemProduto.getProduto().getId().equals(produtoId))
                .findFirst();

        if (produtoRemovido.isEmpty()) {
            throw new RegraDeNegocioException("Esse produto não foi incluído no carrinho.");
        }

        if (produtoRemovido.get().getQuantidade() < quantidade) {
            carrinho.getProdutosAdicionados().remove(produtoRemovido.get());
        }
        else {
            produtoRemovido.get().setQuantidade(
                    produtoRemovido.get().getQuantidade() - quantidade);
        }

        return this.repository.update(carrinho);
    }

    @Override
    public Carrinho limparCarrinho(Long clienteId) {
        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();

        carrinho.getProdutosAdicionados().clear();

        return this.repository.update(carrinho);
    }

    @Override
    public Carrinho visualizarCarrinho(Long clienteId) {
        return this.clienteService.findById(clienteId).getCarrinho();
    }

    @Override
    public Pedido finalizarCompra(Long clienteId) {

        Cliente cliente = clienteService.findById(clienteId);

        Carrinho carrinho = cliente.getCarrinho();

        if (carrinho.getProdutosAdicionados().isEmpty()) {
            throw new RegraDeNegocioException("O carrinho está vazio! Adicione itens para finalizar a compra!");
        }

        List<ItemProduto> produtosAdicionados = carrinho.getProdutosAdicionados();
        Pedido pedido = new Pedido(cliente, produtosAdicionados);

        Pedido pedidoFinalizado = this.pedidoService.fazerPedido(pedido);

        //Se a operação de criação de pedido for um sucesso, o carrinho é limpo
        limparCarrinho(clienteId);

        return pedidoFinalizado;
    }
}
