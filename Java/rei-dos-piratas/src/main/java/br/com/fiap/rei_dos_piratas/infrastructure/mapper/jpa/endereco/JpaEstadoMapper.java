package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.endereco;

import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEstadoEntity;

public class JpaEstadoMapper {

    public static Estado toEntity(JpaEstadoEntity estado){
        if(estado == null){
            return null;
        }

        return new Estado(
                estado.getId(),
                estado.getEstadoNome(),
                estado.getEstadoSigla());
    }

    public static JpaEstadoEntity toJpaEntity(Estado estado){
        if(estado == null){
            return null;
        }

        return new JpaEstadoEntity(
                estado.getId(),
                estado.getNome(),
                estado.getSigla());
    }

    private JpaEstadoMapper() {}
}
