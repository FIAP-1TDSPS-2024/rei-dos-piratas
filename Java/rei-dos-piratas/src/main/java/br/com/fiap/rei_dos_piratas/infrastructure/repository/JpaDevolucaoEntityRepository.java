package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDevolucaoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaDevolucaoEntityRepository extends JpaRepository<JpaDevolucaoEntity, Long> {

    org.springframework.data.domain.Page<JpaDevolucaoEntity> findAllByPedido_Id(Long pedidoId, Pageable pageable);

    org.springframework.data.domain.Page<JpaDevolucaoEntity> findAllByStatus(StatusEnum status, Pageable pageable);

    List<JpaDevolucaoEntity> findAllByPedido_Id(Long pedidoId);

    @Query("SELECT d FROM JpaDevolucaoEntity d WHERE d.id IN :ids AND d.status = :status")
    List<JpaDevolucaoEntity> findByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") StatusEnum status);

    @Modifying
    @Query("UPDATE JpaDevolucaoEntity d SET d.status = :newStatus WHERE d.id IN :ids")
    void updateStatusBatch(@Param("ids") List<Long> ids, @Param("newStatus") StatusEnum newStatus);
}

