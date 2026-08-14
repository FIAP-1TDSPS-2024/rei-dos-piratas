package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

import java.util.List;

public interface DevolucaoRepository {
    Page<Devolucao> listAll(int pageNumber, int pageSize);
    Page<Devolucao> listAllByPedido(int pageNumber, int pageSize, Long pedidoId);
    List<Devolucao> findAllByPedidoId(Long pedidoId);
    Devolucao findById(Long id);
    Devolucao create(Devolucao devolucao);
    Devolucao update(Devolucao devolucao);
    void delete(Long id);
}

