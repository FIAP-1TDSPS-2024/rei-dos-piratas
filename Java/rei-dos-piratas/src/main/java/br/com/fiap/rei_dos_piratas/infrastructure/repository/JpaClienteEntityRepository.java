package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaClienteEntityRepository extends JpaRepository<JpaClienteEntity, Long> {
}
