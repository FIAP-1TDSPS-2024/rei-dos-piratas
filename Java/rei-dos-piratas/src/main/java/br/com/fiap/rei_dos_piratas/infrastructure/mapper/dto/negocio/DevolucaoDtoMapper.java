package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemDevolucaoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;

import java.util.Collections;

public class DevolucaoDtoMapper {

    public static DevolucaoOutDto toDto(Devolucao devolucao) {
        return new DevolucaoOutDto(
                devolucao.getId(),
                devolucao.getPedido() != null ? devolucao.getPedido().getId() : null,
                devolucao.getMotivo() != null ? devolucao.getMotivo().getId() : null,
                devolucao.getMotivo() != null ? devolucao.getMotivo().getCodigo() : null,
                devolucao.getMotivo() != null ? devolucao.getMotivo().getDescricao() : null,
                devolucao.getDescricao(),
                devolucao.getItens() == null ? Collections.emptyList() : devolucao.getItens().stream()
                        .map(item -> new ItemDevolucaoOutDto(
                                item.getId(),
                                item.getItemPedido() != null ? item.getItemPedido().getId() : null,
                                item.getItemPedido() != null && item.getItemPedido().getProduto() != null ? item.getItemPedido().getProduto().getId() : null,
                                item.getItemPedido() != null && item.getItemPedido().getProduto() != null ? item.getItemPedido().getProduto().getNome() : null,
                                item.getQuantidade()))
                        .toList(),
                devolucao.getValorTotal(),
                devolucao.getValorFrete(),
                devolucao.getStatus(),
                devolucao.getDataSolicitacao(),
                devolucao.getDataAprovacao(),
                devolucao.getDataConclusao(),
                devolucao.getAprovada(),
                devolucao.getServicoEntrega(),
                devolucao.getPedidoFrete(),
                devolucao.getProtocoloEnvio(),
                devolucao.getStatusEnvio(),
                devolucao.getTracking(),
                devolucao.getTrackingUrl()
        );
    }

    private DevolucaoDtoMapper() {}
}

