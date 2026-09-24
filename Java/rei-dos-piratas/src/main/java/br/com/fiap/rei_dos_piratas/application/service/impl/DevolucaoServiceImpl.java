package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
public class DevolucaoServiceImpl implements DevolucaoService {

    private final DevolucaoRepository repository;

    private final FreteService freteService;

    public DevolucaoServiceImpl(DevolucaoRepository repository, FreteService freteService) {
        this.repository = repository;
        this.freteService = freteService;
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
    public Page<Devolucao> findAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        log.debug("[SERVICE-DEVOLUCAO] findAllByStatus - status={}, página: {}, tamanho: {}", status, pageNumber, pageSize);
        return repository.listAllByStatus(pageNumber, pageSize, status);
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
        Pedido pedido = devolucao.getPedido();

        //Verificar se há devoluções em aberto para o pedido
        List<Devolucao> devolucoes = this.findAllByPedidoId(pedido.getId());

        //Valida se não há devoluções ativas
        boolean possuiDevolucaoAtiva = devolucoes
                .stream()
                .anyMatch(d ->
                        d.getStatus() != StatusDevolucaoEnum.CANCELADO &&
                        d.getStatus() != StatusDevolucaoEnum.RETORNADO);

        if (possuiDevolucaoAtiva) {
            throw new RegraDeNegocioException("Não é possível solicitar novas devoluções se há uma devolução ativa");
        }

        //Valida se a devolução é por arrependimento, se for, a aprovação é automática
        //As datas são validadas na classe Devolucao.java
        if (devolucao.getMotivo().getArrependimento()){
            this.aprovarDevolucao(devolucao.getId());
        }

        return repository.create(devolucao);
    }

    @Override
    public Devolucao aprovarDevolucao(Long id) {
        log.info("[SERVICE-DEVOLUCAO] Aprovando devolução ID={}", id);
        // Atualiza status e datas da devolução aprovada
        Devolucao devolucao = repository.findById(id);

        devolucao.setAprovada(true);
        devolucao.setDataAprovacao(LocalDate.now());
        devolucao.setStatus(StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);

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

