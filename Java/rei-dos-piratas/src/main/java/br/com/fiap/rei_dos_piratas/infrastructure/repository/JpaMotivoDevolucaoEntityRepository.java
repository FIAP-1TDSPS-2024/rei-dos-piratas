package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaMotivoDevolucaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMotivoDevolucaoEntityRepository extends JpaRepository<JpaMotivoDevolucaoEntity, Long> {

    List<JpaMotivoDevolucaoEntity> findAllByAtivoTrueOrderByDescricaoAsc();
}

