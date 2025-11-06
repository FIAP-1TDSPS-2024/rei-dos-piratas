package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class PedidoControllerImpl implements PedidoController {

    private final PedidoService service;

    private final ClienteService clienteService;

    private final ProdutoService produtoService;

    public PedidoControllerImpl(PedidoService service, ClienteService clienteService, ProdutoService produtoService) {
        this.service = service;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    @Override
    public Page<PedidoOutDto> findAllByCliente(int pageNumber, int pageSize) {
        Page<Pedido> pedidosPage = this.service.findAll(pageNumber, pageSize);

        List<PedidoOutDto> pedidos = pedidosPage
                .pageItems()
                .stream()
                .map(PedidoDtoMapper::toDto)
                .toList();

        return new Page<PedidoOutDto>(
                pedidosPage.numberOfPages(),
                pedidosPage.pageNumber(),
                pedidos);
    }

    @Override
    public PedidoOutDto findById(Long id) {
        Pedido pedido = this.service.findById(id);
        return PedidoDtoMapper.toDto(pedido);
    }

    @Override
    public PedidoOutDto fazerPedido(PedidoInDto pedido) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cliente cliente = clienteService.findById(userDetails.getId());

        List<ItemProdutoPedido> items = pedido
                .produtosAdicionados()
                .stream()
                .map(item ->
                        new ItemProdutoPedido(
                                this.produtoService.findById(item.produtoId()),
                                item.quantidade())).toList();

        Pedido pedidoEntity = PedidoDtoMapper.toEntity(cliente, items);

        return PedidoDtoMapper.toDto(
                this.service.fazerPedido(pedidoEntity));
    }

    @Override
    public PedidoOutDto pagarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.pagarPedido(id));
    }

    @Override
    public PedidoOutDto enviarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.enviarPedido(id));
    }

    @Override
    public PedidoOutDto entregarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.entregarPedido(id));
    }

    @Override
    public PedidoOutDto cancelarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.cancelarPedido(id));
    }
}
