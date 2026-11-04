package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevolucaoService {
    Page<Devolucao> findAll(int pageNumber, int pageSize);
    Page<Devolucao> findAllByPedido(int pageNumber, int pageSize, Long pedidoId);
    Page<Devolucao> findAllByStatus(int pageNumber, int pageSize, StatusDevolucaoEnum status);
    List<Devolucao> findAllByPedidoId(Long pedidoId);
    Optional<Devolucao> findDevolucaoAtivaByPedidoId(Long pedidoId);
    Devolucao findById(Long id);
    Devolucao solicitarDevolucao(Devolucao devolucao);
    Devolucao aprovarDevolucao(Long id);
    String organizarDevolucoesParaEnvio(List<Long> devolucoes);
    Devolucao recusarDevolucao(Long id);
    Devolucao concluirDevolucao(Long id);
    Optional<Devolucao> findByPedidoFrete(UUID pedidoFrete);
    void rastreioDevolucaoWebhook(Devolucao devolucao, EventoRastreio evento);
}
