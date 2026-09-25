package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.MotivoDevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemDevolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.DevolucaoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemDevolucaoInDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DevolucaoControllerImpl implements DevolucaoController {

    private final DevolucaoService service;
    private final MotivoDevolucaoService motivoDevolucaoService;
    private final PedidoService pedidoService;

    public DevolucaoControllerImpl(DevolucaoService service, MotivoDevolucaoService motivoDevolucaoService, PedidoService pedidoService) {
        this.service = service;
        this.motivoDevolucaoService = motivoDevolucaoService;
        this.pedidoService = pedidoService;
    }

    @Override
    public Page<DevolucaoOutDto> findAll(int pageNumber, int pageSize) {
        Page<Devolucao> page = service.findAll(pageNumber, pageSize);
        List<DevolucaoOutDto> items = page.pageItems().stream()
                .map(DevolucaoDtoMapper::toDto)
                .toList();
        return new Page<>(page.numberOfPages(), page.pageNumber(), items);
    }

    @Override
    public Page<DevolucaoOutDto> findAllByPedido(int pageNumber, int pageSize, Long pedidoId) {
        Page<Devolucao> page = service.findAllByPedido(pageNumber, pageSize, pedidoId);
        List<DevolucaoOutDto> items = page.pageItems().stream()
                .map(DevolucaoDtoMapper::toDto)
                .toList();
        return new Page<>(page.numberOfPages(), page.pageNumber(), items);
    }

    @Override
    public Page<DevolucaoOutDto> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        Page<Devolucao> page = service.findAllByStatus(pageNumber, pageSize, status);
        List<DevolucaoOutDto> items = page.pageItems().stream()
                .map(DevolucaoDtoMapper::toDto)
                .toList();
        return new Page<>(page.numberOfPages(), page.pageNumber(), items);
    }

    @Override
    public List<DevolucaoOutDto> findAllByPedidoId(Long pedidoId) {
        return service.findAllByPedidoId(pedidoId).stream()
                .map(DevolucaoDtoMapper::toDto)
                .toList();
    }

    @Override
    public DevolucaoOutDto findById(Long id) {
        return DevolucaoDtoMapper.toDto(service.findById(id));
    }

    @Override
    public DevolucaoOutDto solicitarDevolucao(DevolucaoInDto devolucaoInDto) {
        Pedido pedido = pedidoService.findById(devolucaoInDto.pedidoId());
        MotivoDevolucao motivo = motivoDevolucaoService.findById(devolucaoInDto.motivoId());
        Devolucao devolucao = new Devolucao(
                pedido,
                motivo,
                devolucaoInDto.descricao(),
                mapearItensDevolucao(devolucaoInDto.itens(), pedido));
        return DevolucaoDtoMapper.toDto(service.solicitarDevolucao(devolucao));
    }

    private List<ItemDevolucao> mapearItensDevolucao(List<ItemDevolucaoInDto> itensInDto, Pedido pedido) {
        if (itensInDto == null) {
            return Collections.emptyList();
        }

        Map<Long, ItemProdutoPedido> itensPedidoPorId = pedido.getProdutosAdicionados().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ItemProdutoPedido::getId, Function.identity()));

        return itensInDto.stream()
                .map(itemDto -> {
                    ItemProdutoPedido itemPedido = itensPedidoPorId.get(itemDto.itemPedidoId());
                    if (itemPedido == null) {
                        throw new RegraDeNegocioException("Item do pedido nao encontrado para devolucao.");
                    }

                    Integer quantidade = itemDto.quantidade();
                    if (quantidade == null) {
                        throw new RegraDeNegocioException("Quantidade do item devolvido e obrigatoria.");
                    }

                    return new ItemDevolucao(null, itemPedido, quantidade);
                })
                .toList();
    }

    @Override
    public DevolucaoOutDto aprovarDevolucao(Long id) {
        return DevolucaoDtoMapper.toDto(service.aprovarDevolucao(id));
    }

    @Override
    public DevolucaoOutDto recusarDevolucao(Long id) {
        return DevolucaoDtoMapper.toDto(service.recusarDevolucao(id));
    }

    @Override
    public DevolucaoOutDto concluirDevolucao(Long id) {
        return DevolucaoDtoMapper.toDto(service.concluirDevolucao(id));
    }
}

