package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaPedidoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPedidoEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class PedidoRepositoryImpl implements PedidoRepository {

    private final JpaPedidoEntityRepository repository;

    public PedidoRepositoryImpl(JpaPedidoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Pedido> listAll(int pageNumber, int pageSize) {
        log.debug("[REPO-PEDIDO] Listando todos os pedidos - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Page<Pedido> listAllByClient(int pageNumber, int pageSize, Long clienteId) {
        log.debug("[REPO-PEDIDO] Listando pedidos do cliente ID={} - página: {}, tamanho: {}", clienteId, pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByCliente_Id(clienteId, Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Page<Pedido> listAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        log.debug("[REPO-PEDIDO] Listando pedidos com status={} - página: {}, tamanho: {}", status, pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByStatus(status, Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Pedido findById(Long id) {
        log.debug("[REPO-PEDIDO] findById - ID={}", id);
        return JpaPedidoMapper.toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    public Pedido create(Pedido pedido) {
        log.debug("[REPO-PEDIDO] Persistindo novo pedido no banco");
        Pedido criado = JpaPedidoMapper.toEntity(
                this.repository.save(JpaPedidoMapper.toJpaEntity(pedido)));
        log.info("[REPO-PEDIDO] Pedido criado com sucesso - ID={}", criado.getId());
        return criado;
    }

    @Override
    public Pedido update(Pedido pedido) {
        log.debug("[REPO-PEDIDO] Atualizando pedido ID={}", pedido.getId());
        Optional<JpaPedidoEntity> pedidoExistente = this.repository.findById(pedido.getId());

        if (pedidoExistente.isPresent()) {
            Pedido atualizado = JpaPedidoMapper.toEntity(
                    this.repository.save(JpaPedidoMapper.toJpaEntity(pedido)));
            log.debug("[REPO-PEDIDO] Pedido ID={} atualizado com sucesso", pedido.getId());
            return atualizado;
        } else {
            log.warn("[REPO-PEDIDO] Pedido ID={} não encontrado para atualização", pedido.getId());
            return null;
        }
    }

    @Override
    public Pedido findByPedidoFrete(UUID uuid) {
        log.debug("[REPO-PEDIDO] findByPedidoFrete - UUID={}", uuid);
        return JpaPedidoMapper.toEntity(this.repository.findByPedidoFrete(uuid).orElseThrow());
    }

    @Override
    public List<Pedido> findByIdsAndStatus(List<Long> ids, StatusEnum status) {
        log.debug("[REPO-PEDIDO] findByIdsAndStatus - IDs={}, status={}", ids, status);
        return this.repository.findByIdsAndStatus(ids, status)
                .stream()
                .map(JpaPedidoMapper::toEntity)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatusBatch(List<Long> ids, StatusEnum newStatus) {
        log.info("[REPO-PEDIDO] Atualizando status em lote para {} pedido(s) - novoStatus={}, IDs={}", ids.size(), newStatus, ids);
        this.repository.updateStatusBatch(ids, newStatus);
    }
}
