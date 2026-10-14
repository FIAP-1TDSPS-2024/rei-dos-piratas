package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaEventoExternoEntity;

public class JpaEventoExternoMapper {

    public static EventoExterno toEntity(JpaEventoExternoEntity jpaEntity) {
        return new EventoExterno(
                jpaEntity.getId(),
                jpaEntity.getOrigem(),
                jpaEntity.getIdExterno(),
                jpaEntity.getTipoEvento(),
                jpaEntity.getDataRecebimento(),
                jpaEntity.getStatusProcessamento(),
                jpaEntity.getPayload(),
                jpaEntity.getTentativas(),
                jpaEntity.getProcessandoAte()
        );
    }

    public static JpaEventoExternoEntity toJpaEntity(EventoExterno entity) {
        return new JpaEventoExternoEntity(
                entity.getId(),
                entity.getOrigem(),
                entity.getIdExterno(),
                entity.getTipoEvento(),
                entity.getDataRecebimento(),
                entity.getStatusProcessamento(),
                entity.getPayload(),
                entity.getTentativas(),
                entity.getProcessandoAte()
        );
    }

    private JpaEventoExternoMapper() {
    }
}
