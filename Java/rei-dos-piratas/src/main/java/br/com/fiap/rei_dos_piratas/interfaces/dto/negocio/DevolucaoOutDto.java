package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DevolucaoOutDto(
        Long id,
        Long pedidoId,
        Long motivoId,
        String motivoCodigo,
        String motivoDescricao,
        String descricao,
        List<ItemDevolucaoOutDto> itens,
        BigDecimal valorTotal,
        BigDecimal valorFrete,
        StatusDevolucaoEnum status,
        LocalDate dataSolicitacao,
        LocalDate dataAprovacao,
        LocalDate dataConclusao,
        Boolean aprovada,
        Long servicoEntrega,
        UUID pedidoFrete,
        String protocoloEnvio,
        String statusEnvio,
        String tracking,
        String trackingUrl
) {}

