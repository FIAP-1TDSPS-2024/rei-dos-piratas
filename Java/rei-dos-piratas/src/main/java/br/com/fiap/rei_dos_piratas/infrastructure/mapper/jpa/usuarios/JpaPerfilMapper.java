package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.entity.Role;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                        .map(JpaPerfilMapper::mapRoleWithoutPerfis)
                        .collect(Collectors.toList()) :
                    Collections.emptyList()
        );
    }

    public static JpaPerfilEntity toJpaEntity(Perfil perfil) {
        if (perfil == null) {
            return null;
        }

        List<JpaRoleEntity> jpaRoles = perfil.getRoles() != null ?
            perfil.getRoles().stream()
                .map(JpaPerfilMapper::mapJpaRoleWithoutPerfis)
                .collect(Collectors.toList()) :
            Collections.emptyList();

        return new JpaPerfilEntity(
                perfil.getId(),
                perfil.getNome(),
                perfil.getDescricao(),
                jpaRoles,
                Collections.emptyList() // usuarios - empty list for new entities
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
                Collections.emptyList(), // roles - empty list
                Collections.emptyList()  // usuarios - empty list
        );
    }

    // Private helper methods to avoid circular dependency
    private static Role mapRoleWithoutPerfis(JpaRoleEntity jpaRoleEntity) {
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

    private static JpaRoleEntity mapJpaRoleWithoutPerfis(Role role) {
        if (role == null) {
            return null;
        }
        return new JpaRoleEntity(
                role.getId(),
                role.getNome(),
                role.getDescricao(),
                Collections.emptyList() // perfis - empty list
        );
    }

    private JpaPerfilMapper() {}
}
