package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaFuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFuncionarioEntityRepository extends JpaRepository<JpaFuncionarioEntity, Long> {

    JpaFuncionarioEntity findFirstByEmail(String email);

    JpaFuncionarioEntity findFirstByUserName(String username);
}
