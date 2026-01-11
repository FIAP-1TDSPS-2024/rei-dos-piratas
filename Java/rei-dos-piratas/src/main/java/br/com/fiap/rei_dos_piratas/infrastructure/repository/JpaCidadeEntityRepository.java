package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaCidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCidadeEntityRepository extends JpaRepository<JpaCidadeEntity, Long> {
    JpaCidadeEntity findFirstByCidadeNomeIgnoreCaseAndEstado_EstadoNomeIgnoreCase(String cidadeNome, String estadoEstadoNome);
}
