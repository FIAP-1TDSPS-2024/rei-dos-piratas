package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.CarrinhoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoCarrinhoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

public class CarrinhoControllerImpl implements CarrinhoController {

    private final CarrinhoService service;

    private final ProdutoService produtoService;

    private final EnderecoService enderecoService;

    public CarrinhoControllerImpl(CarrinhoService service, ProdutoService produtoService, EnderecoService enderecoService) {
        this.service = service;
        this.produtoService = produtoService;
        this.enderecoService = enderecoService;
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
    public PedidoOutDto finalizarCompra(PedidoCarrinhoInDto pedidoDto) {

        Endereco enderecoEntrega = this.enderecoService.findById(pedidoDto.EnderecoEntregaId());

        Pedido pedido = this.service.finalizarCompra(enderecoEntrega, pedidoDto.frete());
        return PedidoDtoMapper.toDto(pedido);
    }
}
