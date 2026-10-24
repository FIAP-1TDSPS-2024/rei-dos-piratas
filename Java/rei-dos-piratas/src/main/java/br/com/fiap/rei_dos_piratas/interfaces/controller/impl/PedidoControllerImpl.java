package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio.PagamentoCartaoCreditoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio.PedidoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PixQrCodeResponseDto;

import java.util.List;
import java.util.Map;

public class PedidoControllerImpl implements PedidoController {

    private final PedidoService service;

    private final ClienteService clienteService;

    private final ProdutoService produtoService;

    private final EnderecoService enderecoService;

    private final UsuarioAtualService usuarioAtual;

    public PedidoControllerImpl(PedidoService service, ClienteService clienteService, ProdutoService produtoService, EnderecoService enderecoService, UsuarioAtualService usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
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

        Cliente cliente = clienteService.findById(this.usuarioAtual.identidade().id());

        List<ItemProdutoPedido> items = pedido
                .produtosAdicionados()
                .stream()
                .map(item ->
                        new ItemProdutoPedido(
                                this.produtoService.findById(item.produtoId()),
                                item.quantidade())).toList();

        Endereco enderecoEntrega = this.enderecoService.findById(pedido.EnderecoEntregaId());

        Pedido pedidoEntity = PedidoDtoMapper.toEntity(cliente, enderecoEntrega, items, pedido.freteServiceId(), pedido.tipoPagamento());

        if (pedidoEntity.getTipoPagamento().equals(TipoPagamentoEnum.CREDIT_CARD)) {

            if (pedido.cartao() == null) {
                throw new RegraDeNegocioException("Informe o cartão de crédito para pagamento com cartão");
            }
            else{
                PagamentoCartao pagamentoCartao = PagamentoCartaoCreditoMapper.toEntity(pedido.cartao());

                return PedidoDtoMapper.toDto(
                        this.service.fazerPedido(pedidoEntity, pagamentoCartao));
            }
        }

        return PedidoDtoMapper.toDto(
                this.service.fazerPedido(pedidoEntity));
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
    public PedidoOutDto cancelarPedido(Long id) {
        return PedidoDtoMapper.toDto(
                this.service.cancelarPedido(id));
    }

    @Override
    public PixQrCodeResponseDto obterQrCodeCobrancaPix(Long id) {
        return this.service.obterQrCodeCobrancaPix(id);
    }
}
