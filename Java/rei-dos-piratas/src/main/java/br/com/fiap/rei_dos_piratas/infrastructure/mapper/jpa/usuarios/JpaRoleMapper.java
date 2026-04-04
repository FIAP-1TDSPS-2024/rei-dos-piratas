package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Role;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;

import java.util.Collections;

public class JpaRoleMapper {

    public static Role toEntity(JpaRoleEntity jpaRoleEntity) {
        if (jpaRoleEntity == null) {
            return null;
        }

        return new Role(
                jpaRoleEntity.getId(),
                jpaRoleEntity.getNome(),
                jpaRoleEntity.getDescricao(),
                jpaRoleEntity.getPerfis() != null ?
                    jpaRoleEntity.getPerfis().stream()
                        .map(JpaPerfilMapper::toEntityWithoutRoles)
                        .toList() :
                    Collections.emptyList()
        );
    }

    public static JpaRoleEntity toJpaEntity(Role role) {
        if (role == null) {
            return null;
        }

        return new JpaRoleEntity(
                role.getId(),
                role.getNome(),
                role.getDescricao(),
                role.getPerfis() != null ?
                    role.getPerfis().stream()
                        .map(JpaPerfilMapper::toJpaEntityWithoutRoles)
                        .toList() :
                    Collections.emptyList()
        );
    }

    public static Role toEntityWithoutPerfis(JpaRoleEntity jpaRoleEntity) {
        if (jpaRoleEntity == null) {
            return null;
        }

        return new Role(
                jpaRoleEntity.getId(),
                jpaRoleEntity.getNome(),
                jpaRoleEntity.getDescricao(),
                Collections.emptyList()
        );
    }

    public static JpaRoleEntity toJpaEntityWithoutPerfis(Role role) {
        if (role == null) {
            return null;
        }

        return new JpaRoleEntity(
                role.getId(),
                role.getNome(),
                role.getDescricao(),
                Collections.emptyList()
        );
    }

    private JpaRoleMapper() {}
}
