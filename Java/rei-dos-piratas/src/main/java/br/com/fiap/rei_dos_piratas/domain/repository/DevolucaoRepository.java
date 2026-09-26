package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevolucaoRepository {
    Page<Devolucao> listAll(int pageNumber, int pageSize);
    Page<Devolucao> listAllByPedido(int pageNumber, int pageSize, Long pedidoId);
    Page<Devolucao> listAllByStatus(int pageNumber, int pageSize, StatusDevolucaoEnum status);
    List<Devolucao> findAllByPedidoId(Long pedidoId);
    Devolucao findById(Long id);
    Devolucao create(Devolucao devolucao);
    Devolucao update(Devolucao devolucao);
    Optional<Devolucao> findByPedidoFrete(UUID pedidoFrete);
    List<Devolucao> findByIdsAndStatus(List<Long> ids, StatusDevolucaoEnum status);
    void updateStatusBatch(List<Long> ids, StatusDevolucaoEnum newStatus);
    void delete(Long id);
}

