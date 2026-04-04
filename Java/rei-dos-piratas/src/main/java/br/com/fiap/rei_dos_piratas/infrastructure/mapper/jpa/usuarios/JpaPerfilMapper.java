package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;

public class JpaPerfilMapper {

    public static Perfil toEntity(JpaPerfilEntity jpaPerfilEntity) {
        if (jpaPerfilEntity == null) {
            return null;
        }

        return new Perfil(
                jpaPerfilEntity.getId(),
                jpaPerfilEntity.getNome(),
                jpaPerfilEntity.getDescricao()
        );
    }

    public static JpaPerfilEntity toJpaEntity(Perfil perfil) {
        if (perfil == null) {
            return null;
        }

        return new JpaPerfilEntity(
                perfil.getId(),
                perfil.getNome(),
                perfil.getDescricao()
        );
    }

    private JpaPerfilMapper() {}
}
