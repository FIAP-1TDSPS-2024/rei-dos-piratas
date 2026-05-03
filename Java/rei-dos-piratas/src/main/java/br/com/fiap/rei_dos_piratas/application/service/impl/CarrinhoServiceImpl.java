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
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Carrinho adicionarProduto(ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        // Verifica o estoque
        if (produto.getEstoque() < itemProdutoPedido.getQuantidade()) {
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
            itemAtual.setQuantidade(novaQuantidade);
        } else {
            // 3. Se não existe, cria o item novo
            carrinho.getProdutosAdicionados().add(new ItemProdutoCarrinho(produto, itemProdutoPedido.getQuantidade()));
        }

        return this.repository.update(carrinho);
    }

    @Transactional
    @Override
    public Carrinho removerProduto(ItemProdutoPedido itemProdutoPedido) {
        Produto produto = produtoService.findById(itemProdutoPedido.getProduto().getId());

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        Carrinho carrinho = cliente.getCarrinho();

        // 1. Corrigido o filtro: agora ele olha para o 'item' da lista
        Optional<ItemProdutoCarrinho> produtoRemovido = carrinho
                .getProdutosAdicionados()
                .stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (produtoRemovido.isEmpty()) {
            throw new RegraDeNegocioException("Esse produto não foi incluído no carrinho.");
        }

        ItemProdutoCarrinho itemAtual = produtoRemovido.get();

        // 2. Calculamos a nova quantidade
        int novaQuantidade = itemAtual.getQuantidade() - itemProdutoPedido.getQuantidade();

        // 3. Corrigida a lógica matemática: se zerar ou ficar negativo, remove da lista
        if (novaQuantidade <= 0) {
            // Remove o item diretamente da coleção gerenciada pelo Hibernate
            carrinho.getProdutosAdicionados().remove(itemAtual);
        } else {
            // Se ainda sobrar, apenas atualiza a quantidade
            itemAtual.setQuantidade(novaQuantidade);
        }

        return this.repository.update(carrinho);
    }

    @Transactional
    @Override
    public Carrinho limparCarrinho() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        Carrinho carrinho = cliente.getCarrinho();

        List<ItemProdutoCarrinho> items = new ArrayList<>();

        carrinho.setProdutosAdicionados(items);

        return this.repository.update(carrinho);
    }

    @Override
    public Carrinho visualizarCarrinho() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return this.clienteService.findById(userDetails.getId()).getCarrinho();
    }

    @Transactional
    @Override
    public Pedido finalizarCompra(Endereco enderecoEntrega, Long freteServiceId) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        Carrinho carrinho = cliente.getCarrinho();

        if (carrinho.getProdutosAdicionados().isEmpty()) {
            throw new RegraDeNegocioException("O carrinho está vazio! Adicione itens para finalizar a compra!");
        }

        List<ItemProdutoPedido> produtosAdicionados = carrinho
                                                        .getProdutosAdicionados()
                                                        .stream()
                                                        .map(JpaItemProdutoMapper::toPedido)
                                                        .collect(Collectors.toList());
        Pedido pedido = new Pedido(cliente, enderecoEntrega, produtosAdicionados, freteServiceId);

        Pedido pedidoFinalizado = this.pedidoService.fazerPedido(pedido);

        //Se a operação de criação de pedido for um sucesso, o carrinho é limpo
        limparCarrinho();

        return pedidoFinalizado;
    }
}
