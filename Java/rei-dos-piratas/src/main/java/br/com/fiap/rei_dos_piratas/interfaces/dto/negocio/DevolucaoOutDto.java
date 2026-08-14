package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.MotivoDevolucaoEnum;

import java.time.LocalDate;

public record DevolucaoOutDto(
        Long id,
        Long pedidoId,
        MotivoDevolucaoEnum motivo,
        String motivoDescricao,
        String descricao,
        LocalDate dataSolicitacao,
        LocalDate dataAprovacao,
        LocalDate dataConclusao,
        Boolean aprovada
) {}

