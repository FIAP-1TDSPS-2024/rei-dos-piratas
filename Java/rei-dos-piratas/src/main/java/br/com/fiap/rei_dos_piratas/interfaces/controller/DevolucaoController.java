package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;

import java.util.List;
import java.util.Map;

public interface DevolucaoController {
    Page<DevolucaoOutDto> findAll(int pageNumber, int pageSize);
    Page<DevolucaoOutDto> findAllByPedido(int pageNumber, int pageSize, Long pedidoId);
    Page<DevolucaoOutDto> findAllByStatus(int pageNumber, int pageSize, StatusDevolucaoEnum status);
    List<DevolucaoOutDto> findAllByPedidoId(Long pedidoId);
    DevolucaoOutDto findById(Long id);
    DevolucaoOutDto solicitarDevolucao(DevolucaoInDto devolucaoInDto);
    DevolucaoOutDto aprovarDevolucao(Long id);
    DevolucaoOutDto recusarDevolucao(Long id);
    DevolucaoOutDto concluirDevolucao(Long id);
}

