package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaPedidoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPedidoEntityRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PedidoRepositoryImpl implements PedidoRepository {

    private final JpaPedidoEntityRepository repository;

    public PedidoRepositoryImpl(JpaPedidoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Pedido> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Page<Pedido> listAllByClient(int pageNumber, int pageSize, Long clienteId) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByCliente_Id(
                        clienteId,
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Page<Pedido> listAllByStatus(int pageNumber, int pageSize, StatusEnum status) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByStatus(
                        status,
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaPedidoMapper::toEntity));
    }

    @Override
    public Pedido findById(Long id) {
        return JpaPedidoMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Pedido create(Pedido pedido) {
        return JpaPedidoMapper.toEntity(
                this.repository.save(
                        JpaPedidoMapper.toJpaEntity(pedido)));
    }

    @Override
    public Pedido update(Pedido pedido) {
        Optional<JpaPedidoEntity> pedidoExistente = this.repository.findById(pedido.getId());

        if (pedidoExistente.isPresent()) {
            return JpaPedidoMapper.toEntity(
                    this.repository.save(
                            JpaPedidoMapper.toJpaEntity(pedido)));
        }
        else{
            return null;
        }
    }

    @Override
    public Pedido findByPedidoFrete(UUID uuid) {
        return JpaPedidoMapper.toEntity(
                this.repository
                        .findByPedidoFrete(uuid)
                        .orElseThrow());
    }

    @Override
    public List<Pedido> findByIdsAndStatus(List<Long> ids, StatusEnum status) {
        return this.repository.findByIdsAndStatus(ids, status)
                .stream()
                .map(JpaPedidoMapper::toEntity)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatusBatch(List<Long> ids, StatusEnum newStatus) {
        this.repository.updateStatusBatch(ids, newStatus);
    }
}
