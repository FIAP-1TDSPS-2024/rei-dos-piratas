package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Role;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;

public class JpaRoleMapper {

    public static Role toEntity(JpaRoleEntity jpaRoleEntity) {
        if (jpaRoleEntity == null) {
            return null;
        }

        return new Role(
                jpaRoleEntity.getId(),
                jpaRoleEntity.getNome(),
                jpaRoleEntity.getDescricao()
        );
    }

    public static JpaRoleEntity toJpaEntity(Role role) {
        if (role == null) {
            return null;
        }

        return new JpaRoleEntity(
                role.getId(),
                role.getNome(),
                role.getDescricao()
        );
    }

    private JpaRoleMapper() {}
}
