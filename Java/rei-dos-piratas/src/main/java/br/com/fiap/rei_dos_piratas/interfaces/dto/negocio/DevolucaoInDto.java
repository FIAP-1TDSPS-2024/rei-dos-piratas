package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.MotivoDevolucaoEnum;

public record DevolucaoInDto(
        Long pedidoId,
        MotivoDevolucaoEnum motivo,
        String descricao
) {}

