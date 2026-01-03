package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDadosEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDadosEmpresaEntityRepository extends JpaRepository<JpaDadosEmpresaEntity, Long> {
}
