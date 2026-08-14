package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;

public class DevolucaoDtoMapper {

    public static DevolucaoOutDto toDto(Devolucao devolucao) {
        return new DevolucaoOutDto(
                devolucao.getId(),
                devolucao.getPedido() != null ? devolucao.getPedido().getId() : null,
                devolucao.getMotivo(),
                devolucao.getMotivo() != null ? devolucao.getMotivo().getDescricao() : null,
                devolucao.getDescricao(),
                devolucao.getDataSolicitacao(),
                devolucao.getDataAprovacao(),
                devolucao.getDataConclusao(),
                devolucao.getAprovada()
        );
    }

    private DevolucaoDtoMapper() {}
}

