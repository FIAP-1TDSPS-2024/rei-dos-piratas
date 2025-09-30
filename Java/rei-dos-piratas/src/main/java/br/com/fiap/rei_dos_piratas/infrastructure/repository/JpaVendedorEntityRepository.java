package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaVendedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaVendedorEntityRepository extends JpaRepository<JpaVendedorEntity, Long> {
}
