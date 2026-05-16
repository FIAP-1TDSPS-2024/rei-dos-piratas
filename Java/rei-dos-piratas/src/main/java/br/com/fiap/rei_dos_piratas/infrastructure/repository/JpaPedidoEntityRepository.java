package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaPedidoEntityRepository extends JpaRepository<JpaPedidoEntity, Long> {

    Page<JpaPedidoEntity> findAllByCliente_Id(Long clienteId, Pageable pageable);

    Page<JpaPedidoEntity> findAllByStatus(StatusEnum status, Pageable pageable);

    Optional<JpaPedidoEntity> findByPedidoFrete(UUID pedidoFrete);

    // Batch operations for performance optimization
    @Query("SELECT p FROM JpaPedidoEntity p WHERE p.id IN :ids AND p.status = :status")
    List<JpaPedidoEntity> findByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") StatusEnum status);

    @Modifying
    @Query("UPDATE JpaPedidoEntity p SET p.status = :newStatus WHERE p.id IN :ids")
    void updateStatusBatch(@Param("ids") List<Long> ids, @Param("newStatus") StatusEnum newStatus);
}
