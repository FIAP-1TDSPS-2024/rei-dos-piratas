package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoleEntityRepository extends JpaRepository<JpaRoleEntity, Long> {
    JpaRoleEntity findFirstByNomeIgnoreCase(String nome);
}
