package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.CarrinhoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.ItemProdutoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

public class CarrinhoControllerImpl implements CarrinhoController {

    private final CarrinhoService service;

    public CarrinhoControllerImpl(CarrinhoService service) {
        this.service = service;
    }

    @Override
    public CarrinhoOutDto adicionarProduto(Long clienteId, Long produtoId, int quantidade) {

        Carrinho carrinho = this.service.adicionarProduto(clienteId, produtoId, quantidade);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto removerProduto(Long clienteId, Long produtoId, int quantidade) {
        Carrinho carrinho = this.service.removerProduto(clienteId, produtoId, quantidade);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto limparCarrinho(Long clienteId) {
        Carrinho carrinho = this.service.limparCarrinho(clienteId);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public CarrinhoOutDto visualizarCarrinho(Long clienteId) {
        Carrinho carrinho = this.service.visualizarCarrinho(clienteId);
        return CarrinhoDtoMapper.toDto(carrinho);
    }

    @Override
    public PedidoOutDto finalizarCompra(Long clienteId) {
        Pedido pedido = this.service.finalizarCompra(clienteId);
        return PedidoDtoMapper.toDto(pedido);
    }
}
