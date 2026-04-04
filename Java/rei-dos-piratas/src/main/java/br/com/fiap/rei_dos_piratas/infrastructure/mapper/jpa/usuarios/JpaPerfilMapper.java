package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;

import java.util.Collections;

public class JpaPerfilMapper {

    public static Perfil toEntity(JpaPerfilEntity jpaPerfilEntity) {
        if (jpaPerfilEntity == null) {
            return null;
        }

        return new Perfil(
                jpaPerfilEntity.getId(),
                jpaPerfilEntity.getNome(),
                jpaPerfilEntity.getDescricao(),
                jpaPerfilEntity.getRoles() != null ?
                    jpaPerfilEntity.getRoles().stream()
                        .map(JpaRoleMapper::toEntityWithoutPerfis)
                        .toList() :
                    Collections.emptyList()
        );
    }

    public static JpaPerfilEntity toJpaEntity(Perfil perfil) {
        if (perfil == null) {
            return null;
        }

        return new JpaPerfilEntity(
                perfil.getId(),
                perfil.getNome(),
                perfil.getDescricao(),
                perfil.getRoles() != null ?
                    perfil.getRoles().stream()
                        .map(JpaRoleMapper::toJpaEntityWithoutPerfis)
                        .toList() :
                    Collections.emptyList()
        );
    }

    public static Perfil toEntityWithoutRoles(JpaPerfilEntity jpaPerfilEntity) {
        if (jpaPerfilEntity == null) {
            return null;
        }

        return new Perfil(
                jpaPerfilEntity.getId(),
                jpaPerfilEntity.getNome(),
                jpaPerfilEntity.getDescricao(),
                Collections.emptyList()
        );
    }

    public static JpaPerfilEntity toJpaEntityWithoutRoles(Perfil perfil) {
        if (perfil == null) {
            return null;
        }

        return new JpaPerfilEntity(
                perfil.getId(),
                perfil.getNome(),
                perfil.getDescricao(),
                Collections.emptyList()
        );
    }

    private JpaPerfilMapper() {}
}
