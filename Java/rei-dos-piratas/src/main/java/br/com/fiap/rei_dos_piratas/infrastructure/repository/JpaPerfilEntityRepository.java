package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaPerfilEntityRepository extends JpaRepository<JpaPerfilEntity, Long> {
    JpaPerfilEntity findFirstByNomeIgnoreCase(String nome);

    @Query("SELECT p FROM JpaPerfilEntity p LEFT JOIN FETCH p.roles WHERE p.id = :id")
    Optional<JpaPerfilEntity> findByIdWithRoles(@Param("id") Long id);

    @Query("SELECT p FROM JpaPerfilEntity p LEFT JOIN FETCH p.roles WHERE UPPER(p.nome) = UPPER(:nome)")
    Optional<JpaPerfilEntity> findByNomeIgnoreCaseWithRoles(@Param("nome") String nome);
}
