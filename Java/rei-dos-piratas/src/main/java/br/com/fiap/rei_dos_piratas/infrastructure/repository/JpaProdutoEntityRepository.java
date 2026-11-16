package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProdutoEntityRepository extends JpaRepository<JpaProdutoEntity, Long> {

    org.springframework.data.domain.Page<JpaProdutoEntity> findAllByAtivoTrue(org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<JpaProdutoEntity> findAllByAtivoFalse(org.springframework.data.domain.Pageable pageable);
}
