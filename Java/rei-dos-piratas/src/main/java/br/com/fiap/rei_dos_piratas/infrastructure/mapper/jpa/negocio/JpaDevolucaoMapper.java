package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDevolucaoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;

public class JpaDevolucaoMapper {

    public static Devolucao toEntity(JpaDevolucaoEntity entity) {
        return new Devolucao(
                entity.getId(),
                JpaPedidoMapper.toEntity(entity.getPedido()),
                entity.getMotivo(),
                entity.getDescricao(),
                entity.getDataSolicitacao(),
                entity.getDataAprovacao(),
                entity.getDataConclusao(),
                entity.getAprovada()
        );
    }

    public static JpaDevolucaoEntity toJpaEntity(Devolucao devolucao) {
        JpaPedidoEntity jpaPedido = JpaPedidoMapper.toJpaEntity(devolucao.getPedido());

        return new JpaDevolucaoEntity(
                devolucao.getId(),
                jpaPedido,
                devolucao.getMotivo(),
                devolucao.getDescricao(),
                devolucao.getDataSolicitacao(),
                devolucao.getDataAprovacao(),
                devolucao.getDataConclusao(),
                devolucao.getAprovada()
        );
    }

    private JpaDevolucaoMapper() {}
}

