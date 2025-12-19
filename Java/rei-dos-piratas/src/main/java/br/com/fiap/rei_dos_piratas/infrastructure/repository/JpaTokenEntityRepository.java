package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTokenEntityRepository extends JpaRepository<JpaTokenEntity, Long> {

    public JpaTokenEntity findFirstOrderByDataCriacaoDesc();
}
