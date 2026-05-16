package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;

import java.util.List;
import java.util.UUID;

public interface PedidoRepository {
    Page<Pedido> listAll(int pageNumber, int pageSize);
    Page<Pedido> listAllByClient(int pageNumber, int pageSize, Long clienteId);
    Page<Pedido> listAllByStatus(int pageNumber, int pageSize, StatusEnum status);
    Pedido findById(Long id);
    Pedido create(Pedido pedido);
    Pedido update(Pedido pedido);
    Pedido findByPedidoFrete(UUID uuid);

    // Métodos de batch para otimização de performance
    List<Pedido> findByIdsAndStatus(List<Long> ids, StatusEnum status);
    void updateStatusBatch(List<Long> ids, StatusEnum newStatus);
}
