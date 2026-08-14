package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;

import java.util.List;

public interface DevolucaoService {
    Page<Devolucao> findAll(int pageNumber, int pageSize);
    Page<Devolucao> findAllByPedido(int pageNumber, int pageSize, Long pedidoId);
    List<Devolucao> findAllByPedidoId(Long pedidoId);
    Devolucao findById(Long id);
    Devolucao solicitarDevolucao(Devolucao devolucao);
    Devolucao aprovarDevolucao(Long id);
    Devolucao recusarDevolucao(Long id);
    Devolucao concluirDevolucao(Long id);
}

