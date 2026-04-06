package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaFuncionarioEntityRepository extends JpaRepository<JpaFuncionarioEntity, Long> {

    JpaFuncionarioEntity findFirstByEmail(String email);

    JpaFuncionarioEntity findFirstByUserName(String username);

    JpaFuncionarioEntity findByUserName(String username);

    Page<JpaFuncionarioEntity> findAllByUsuarioAtivoTrue(Pageable pageable);

    /** Busca com perfil e roles carregados — usado exclusivamente na autenticação. */
    @Query("SELECT f FROM JpaFuncionarioEntity f " +
           "LEFT JOIN FETCH f.perfil p " +
           "LEFT JOIN FETCH p.roles " +
           "WHERE f.email = :email")
    Optional<JpaFuncionarioEntity> findByEmailWithRoles(@Param("email") String email);
}
