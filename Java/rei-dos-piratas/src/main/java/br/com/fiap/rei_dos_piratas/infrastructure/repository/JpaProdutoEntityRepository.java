package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProdutoEntityRepository extends JpaRepository<JpaProdutoEntity, Long> {
}
