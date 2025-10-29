package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaPedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPedidoEntityRepository extends JpaRepository<JpaPedidoEntity, Long> {

    Page<JpaPedidoEntity> findAllByCliente_Id(Long clienteId, Pageable pageable);
}
