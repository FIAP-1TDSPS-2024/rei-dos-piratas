package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDevolucaoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaMotivoDevolucaoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;

import java.util.Collections;

public class JpaDevolucaoMapper {

    public static Devolucao toEntity(JpaDevolucaoEntity entity) {
        Devolucao devolucao = new Devolucao(
                entity.getId(),
                JpaPedidoMapper.toEntity(entity.getPedido()),
                JpaMotivoDevolucaoMapper.toEntity(entity.getMotivo()),
                entity.getDescricao(),
                entity.getItens() == null ? Collections.emptyList() : entity.getItens().stream()
                        .map(JpaItemDevolucaoMapper::toEntity)
                        .toList(),
                entity.getDataSolicitacao(),
                entity.getDataAprovacao(),
                entity.getDataConclusao(),
                entity.getAprovada());

        devolucao.setValorTotal(entity.getValorTotal());
        devolucao.setValorFrete(entity.getValorFrete());
        devolucao.setStatus(entity.getStatus());
        devolucao.setServicoEntrega(entity.getServicoEntrega());
        devolucao.setPedidoFrete(entity.getPedidoFrete());
        devolucao.setProtocoloEnvio(entity.getProtocoloEnvio());
        devolucao.setStatusEnvio(entity.getStatusEnvio());
        devolucao.setTracking(entity.getTracking());
        devolucao.setTrackingUrl(entity.getTrackingUrl());
        return devolucao;
    }

    public static JpaDevolucaoEntity toJpaEntity(Devolucao devolucao) {
        JpaPedidoEntity jpaPedido = JpaPedidoMapper.toJpaEntity(devolucao.getPedido());
        JpaMotivoDevolucaoEntity jpaMotivo = new JpaMotivoDevolucaoEntity();
        if (devolucao.getMotivo() != null) {
            jpaMotivo.setId(devolucao.getMotivo().getId());
        }

        JpaDevolucaoEntity jpaDevolucao = new JpaDevolucaoEntity(
                devolucao.getId(),
                jpaPedido,
                jpaMotivo,
                devolucao.getDescricao(),
                devolucao.getItens() == null ? Collections.emptyList() : devolucao.getItens().stream()
                        .map(JpaItemDevolucaoMapper::toJpaEntity)
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
                devolucao.getTrackingUrl());


        jpaDevolucao.getItens().forEach(item -> item.setDevolucao(jpaDevolucao));
        return jpaDevolucao;
    }

    private JpaDevolucaoMapper() {}
}

