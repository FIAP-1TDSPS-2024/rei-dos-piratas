package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaMotivoDevolucaoEntity;

public class JpaMotivoDevolucaoMapper {

    public static MotivoDevolucao toEntity(JpaMotivoDevolucaoEntity jpaEntity) {
        return new MotivoDevolucao(
                jpaEntity.getId(),
                jpaEntity.getCodigo(),
                jpaEntity.getDescricao(),
                jpaEntity.getArrependimento()
        );
    }

    public static JpaMotivoDevolucaoEntity toJpaEntity(MotivoDevolucao entity) {
        return new JpaMotivoDevolucaoEntity(
                entity.getId(),
                entity.getCodigo(),
                entity.getDescricao(),
                Boolean.TRUE.equals(entity.getArrependimento()),
                true
        );
    }

    private JpaMotivoDevolucaoMapper() {
    }
}

