package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaDevolucaoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaDevolucaoEntityRepository;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.*;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
public class DevolucaoRepositoryImpl implements DevolucaoRepository {

    private final JpaDevolucaoEntityRepository repository;
    private final EntityManager entityManager;

    public DevolucaoRepositoryImpl(JpaDevolucaoEntityRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    private JpaDevolucaoEntity comReferenciasGerenciadas(Devolucao devolucao) {
        JpaDevolucaoEntity entity = JpaDevolucaoMapper.toJpaEntity(devolucao);
        entity.setPedido(entityManager.getReference(JpaPedidoEntity.class, devolucao.getPedido().getId()));
        entity.setMotivo(entityManager.getReference(JpaMotivoDevolucaoEntity.class, devolucao.getMotivo().getId()));
        entity.getItens().forEach(item -> item.setItemPedido(
                entityManager.getReference(JpaProdutosPedidoEntity.class, item.getItemPedido().getId())));
        return entity;
    }

    @Override
    public Page<Devolucao> listAll(int pageNumber, int pageSize) {
        log.debug("[REPO-DEVOLUCAO] Listando todas as devoluções - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaDevolucaoMapper::toEntity));
    }

    @Override
    public Page<Devolucao> listAllByPedido(int pageNumber, int pageSize, Long pedidoId) {
        log.debug("[REPO-DEVOLUCAO] Listando devoluções do pedido ID={} - página: {}, tamanho: {}", pedidoId, pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByPedido_Id(pedidoId, Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaDevolucaoMapper::toEntity));
    }

    @Override
    public Page<Devolucao> listAllByStatus(int pageNumber, int pageSize, StatusDevolucaoEnum status) {
        log.debug("[REPO-DEVOLUCAO] Listando devoluções com status={} - página: {}, tamanho: {}", status, pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByStatus(status, Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaDevolucaoMapper::toEntity));
    }

    @Override
    public List<Devolucao> findAllByPedidoId(Long pedidoId) {
        log.debug("[REPO-DEVOLUCAO] findAllByPedidoId - pedidoId={}", pedidoId);
        return this.repository.findAllByPedido_Id(pedidoId)
                .stream()
                .map(JpaDevolucaoMapper::toEntity)
                .toList();
    }

    @Override
    public Devolucao findById(Long id) {
        log.debug("[REPO-DEVOLUCAO] findById - ID={}", id);
        return JpaDevolucaoMapper.toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public Devolucao create(Devolucao devolucao) {
        log.debug("[REPO-DEVOLUCAO] Persistindo nova devolução para pedido ID={}", devolucao.getPedido().getId());
        Devolucao criada = JpaDevolucaoMapper.toEntity(
                this.repository.save(comReferenciasGerenciadas(devolucao)));
        log.info("[REPO-DEVOLUCAO] Devolução criada com sucesso - ID={}, pedidoId={}", criada.getId(), criada.getPedido().getId());
        return criada;
    }

    @Override
    @Transactional
    public Devolucao update(Devolucao devolucao) {
        log.debug("[REPO-DEVOLUCAO] Atualizando devolução ID={}", devolucao.getId());
        if (this.repository.findById(devolucao.getId()).isPresent()) {
            Devolucao atualizada = JpaDevolucaoMapper.toEntity(
                    this.repository.save(comReferenciasGerenciadas(devolucao)));
            log.debug("[REPO-DEVOLUCAO] Devolução ID={} atualizada com sucesso", devolucao.getId());
            return atualizada;
        } else {
            log.warn("[REPO-DEVOLUCAO] Devolução ID={} não encontrada para atualização", devolucao.getId());
            return null;
        }
    }

    @Override
    public Optional<Devolucao> findByPedidoFrete(UUID pedidoFrete) {
        log.debug("[REPO-DEVOLUCAO] findByPedidoFrete - UUID={}", pedidoFrete);
        return this.repository.findByPedidoFrete(pedidoFrete)
                .map(JpaDevolucaoMapper::toEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("[REPO-DEVOLUCAO] Deletando devolução ID={}", id);
        this.repository.deleteById(id);
        log.info("[REPO-DEVOLUCAO] Devolução ID={} deletada com sucesso", id);
    }

    @Override
    public List<Devolucao> findByIdsAndStatus(List<Long> ids, StatusDevolucaoEnum status) {
        log.debug("[REPO-DEVOLUCAO] Buscando devoluções por IDs={} e status={}", ids, status);
        return this.repository.findByIdsAndStatus(ids, status)
                .stream()
                .map(JpaDevolucaoMapper::toEntity)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatusBatch(List<Long> ids, StatusDevolucaoEnum newStatus) {
        log.info("[REPO-DEVOLUCAO] Atualizando status em lote para {} devolução(ões) - novoStatus={}, IDs={}", ids.size(), newStatus, ids);
        this.repository.updateStatusBatch(ids, newStatus);
    }
}
