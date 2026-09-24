package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import java.util.List;

public record DevolucaoInDto(
        Long pedidoId,
        Long motivoId,
        String descricao,
        Long servicoEntrega,
        List<ItemDevolucaoInDto> itens
) {}

