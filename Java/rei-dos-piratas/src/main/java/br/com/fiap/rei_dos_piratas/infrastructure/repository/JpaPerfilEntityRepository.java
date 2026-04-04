package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPerfilEntityRepository extends JpaRepository<JpaPerfilEntity, Long> {
    JpaPerfilEntity findFirstByNomeIgnoreCase(String nome);
}
