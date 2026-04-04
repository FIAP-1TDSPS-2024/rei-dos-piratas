package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaRoleEntityRepository extends JpaRepository<JpaRoleEntity, Long> {
    JpaRoleEntity findFirstByNomeIgnoreCase(String nome);

    @Query("SELECT r FROM JpaRoleEntity r LEFT JOIN FETCH r.perfis WHERE r.id = :id")
    Optional<JpaRoleEntity> findByIdWithPerfis(@Param("id") Long id);

    @Query("SELECT r FROM JpaRoleEntity r LEFT JOIN FETCH r.perfis WHERE UPPER(r.nome) = UPPER(:nome)")
    Optional<JpaRoleEntity> findByNomeIgnoreCaseWithPerfis(@Param("nome") String nome);
}
