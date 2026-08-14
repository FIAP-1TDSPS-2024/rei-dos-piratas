package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.DevolucaoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;

import java.util.List;

public class DevolucaoControllerImpl implements DevolucaoController {

    private final DevolucaoService service;
    private final PedidoService pedidoService;

    public DevolucaoControllerImpl(DevolucaoService service, PedidoService pedidoService) {
        this.service = service;
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
        Devolucao devolucao = new Devolucao(pedido, devolucaoInDto.motivo(), devolucaoInDto.descricao());
        return DevolucaoDtoMapper.toDto(service.solicitarDevolucao(devolucao));
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

