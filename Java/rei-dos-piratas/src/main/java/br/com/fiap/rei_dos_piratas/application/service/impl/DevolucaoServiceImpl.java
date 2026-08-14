package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DevolucaoServiceImpl implements DevolucaoService {

    private final DevolucaoRepository repository;

    public DevolucaoServiceImpl(DevolucaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Devolucao> findAll(int pageNumber, int pageSize) {
        log.debug("[SERVICE-DEVOLUCAO] findAll - página: {}, tamanho: {}", pageNumber, pageSize);
        return repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Page<Devolucao> findAllByPedido(int pageNumber, int pageSize, Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByPedido - pedidoId={}, página: {}, tamanho: {}", pedidoId, pageNumber, pageSize);
        return repository.listAllByPedido(pageNumber, pageSize, pedidoId);
    }

    @Override
    public List<Devolucao> findAllByPedidoId(Long pedidoId) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByPedidoId - pedidoId={}", pedidoId);
        return repository.findAllByPedidoId(pedidoId);
    }

    @Override
    public Devolucao findById(Long id) {
        log.debug("[SERVICE-DEVOLUCAO] findById - ID={}", id);
        return repository.findById(id);
    }

    @Override
    public Devolucao solicitarDevolucao(Devolucao devolucao) {
        log.info("[SERVICE-DEVOLUCAO] Solicitando devolução para pedido ID={}, motivo={}", devolucao.getPedido().getId(), devolucao.getMotivo());
        // Regras de negócio serão implementadas aqui
        return repository.create(devolucao);
    }

    @Override
    public Devolucao aprovarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Aprovando devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }

    @Override
    public Devolucao recusarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Recusando devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }

    @Override
    public Devolucao concluirDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Concluindo devolução ID={}", id);
        // Regras de negócio serão implementadas aqui
        Devolucao devolucao = repository.findById(id);
        return repository.update(devolucao);
    }
}

