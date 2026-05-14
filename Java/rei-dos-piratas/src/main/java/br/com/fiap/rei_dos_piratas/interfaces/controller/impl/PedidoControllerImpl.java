package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

public class PedidoControllerImpl implements PedidoController {

    private final PedidoService service;

    private final ClienteService clienteService;

    private final ProdutoService produtoService;

    private final EnderecoService enderecoService;

    public PedidoControllerImpl(PedidoService service, ClienteService clienteService, ProdutoService produtoService, EnderecoService enderecoService) {
        this.service = service;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.enderecoService = enderecoService;
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
    public Page<PedidoOutDto> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        Page<Pedido> pedidosPage = this.service.findAllByStatus(pageNumber, pageSize, status);

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

        Endereco enderecoEntrega = this.enderecoService.findById(pedido.EnderecoEntregaId());

        Pedido pedidoEntity = PedidoDtoMapper.toEntity(cliente, enderecoEntrega, items, pedido.freteServiceId());

        return PedidoDtoMapper.toDto(
                this.service.fazerPedido(pedidoEntity));
    }

    @Override
    public PedidoOutDto pagarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.pagarPedido(id));
    }

    @Override
    public String organizarPedidosParaEnvio(List<Long> pedidos) {
        return this.service.organizarPedidosParaEnvio(pedidos);
    }

    @Override
    public Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos) {
        return this.service.gerarEtiquetasParaEnvio(pedidos);
    }

    @Override
    public String imprimirEtiquetasEnvio(List<Long> pedidos) {
        return this.service.imprimirEtiquetasEnvio(pedidos);
    }

    @Override
    public void rastreioPedidoWebhook(String signature, String rawBody) {
        this.service.rastreioPedidoWebhook(signature, rawBody);
    }

    @Override
    public PedidoOutDto cancelarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.cancelarPedido(id));
    }
}
