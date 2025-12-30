package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.CarrinhoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

public class CarrinhoControllerImpl implements CarrinhoController {

    private final CarrinhoService service;

    private final ProdutoService produtoService;

    public CarrinhoControllerImpl(CarrinhoService service, ProdutoService produtoService) {
        this.service = service;
        this.produtoService = produtoService;
    }

    @Override
    public CarrinhoOutDto adicionarProduto(ItemProdutoInDto itemProduto) {

        Produto produto = this.produtoService.findById(itemProduto.produtoId());
        ItemProdutoPedido item = new ItemProdutoPedido(produto, itemProduto.quantidade());

        Carrinho carrinho = this.service.adicionarProduto(item);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto removerProduto(ItemProdutoInDto itemProduto) {

        Produto produto = this.produtoService.findById(itemProduto.produtoId());
        ItemProdutoPedido item = new ItemProdutoPedido(produto, itemProduto.quantidade());

        Carrinho carrinho = this.service.removerProduto(item);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto limparCarrinho() {
        Carrinho carrinho = this.service.limparCarrinho();
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto visualizarCarrinho() {
        Carrinho carrinho = this.service.visualizarCarrinho();
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public PedidoOutDto finalizarCompra() {
        Pedido pedido = this.service.finalizarCompra();
        return PedidoDtoMapper.toDto(pedido);
    }
}
