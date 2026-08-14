package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDevolucaoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaDevolucaoEntityRepository extends JpaRepository<JpaDevolucaoEntity, Long> {

    org.springframework.data.domain.Page<JpaDevolucaoEntity> findAllByPedido_Id(Long pedidoId, Pageable pageable);

    List<JpaDevolucaoEntity> findAllByPedido_Id(Long pedidoId);
}

