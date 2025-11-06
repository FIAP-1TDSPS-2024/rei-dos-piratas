package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaFuncionarioEntityRepository extends JpaRepository<JpaFuncionarioEntity, Long> {

    JpaFuncionarioEntity findFirstByEmail(String email);

    JpaFuncionarioEntity findFirstByUserName(String username);

    JpaFuncionarioEntity findByUserName(String username);

    Page<JpaFuncionarioEntity> findAllByUsuarioAtivoTrue(Pageable pageable);
}
