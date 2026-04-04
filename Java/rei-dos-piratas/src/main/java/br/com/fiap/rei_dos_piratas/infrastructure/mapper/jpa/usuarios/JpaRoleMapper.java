package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.entity.Role;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                        .map(JpaRoleMapper::mapPerfilWithoutRoles)
                        .collect(Collectors.toList()) :
                    Collections.emptyList()
        );
    }

    public static JpaRoleEntity toJpaEntity(Role role) {
        if (role == null) {
            return null;
        }

        List<JpaPerfilEntity> jpaPerfis = role.getPerfis() != null ?
            role.getPerfis().stream()
                .map(JpaRoleMapper::mapJpaPerfilWithoutRoles)
                .collect(Collectors.toList()) :
            Collections.emptyList();

        return new JpaRoleEntity(
                role.getId(),
                role.getNome(),
                role.getDescricao(),
                jpaPerfis
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

    // Private helper methods to avoid circular dependency
    private static Perfil mapPerfilWithoutRoles(JpaPerfilEntity jpaPerfilEntity) {
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

    private static JpaPerfilEntity mapJpaPerfilWithoutRoles(Perfil perfil) {
        if (perfil == null) {
            return null;
        }
        return new JpaPerfilEntity(
                perfil.getId(),
                perfil.getNome(),
                perfil.getDescricao(),
                Collections.emptyList(), // roles - empty list
                Collections.emptyList()  // usuarios - empty list
        );
    }

    private JpaRoleMapper() {}
}
