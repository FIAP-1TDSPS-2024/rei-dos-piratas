package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCartaoClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCartaoClienteEntityRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
public class CartaoClienteRepositoryImpl implements CartaoClienteRepository {

    private final JpaCartaoClienteEntityRepository repository;
    private final EntityManager entityManager;

    public CartaoClienteRepositoryImpl(JpaCartaoClienteEntityRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public CartaoCliente findById(Long id) {
        log.debug("[REPO-CARTAO] findById - ID={}", id);
        return JpaCartaoClienteMapper.toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    public Optional<CartaoCliente> findByIdOptional(Long id) {
        log.debug("[REPO-CARTAO] findByIdOptional - ID={}", id);
        return this.repository.findById(id).map(JpaCartaoClienteMapper::toEntity);
    }

    @Override
    public Optional<CartaoCliente> findByClienteIdAndToken(Long clienteId, String token) {
        log.debug("[REPO-CARTAO] findByClienteIdAndToken - ClienteID={}", clienteId);
        return this.repository.findByCliente_IdAndToken(clienteId, token).map(JpaCartaoClienteMapper::toEntity);
    }

    @Override
    @Transactional
    public CartaoCliente create(CartaoCliente cartao) {
        // Sem dados do cartão no log (token, dígitos ou bandeira)
        log.debug("[REPO-CARTAO] Gravando cartão do cliente ID={}", cartao.getCliente().getId());
        JpaClienteEntity cliente = this.entityManager.getReference(JpaClienteEntity.class, cartao.getCliente().getId());
        CartaoCliente criado = JpaCartaoClienteMapper.toEntity(
                this.repository.save(JpaCartaoClienteMapper.toJpaEntity(cartao, cliente)));
        log.info("[REPO-CARTAO] Cartão gravado - ID={}", criado.getId());
        return criado;
    }

    @Override
    public List<CartaoCliente> findAllByClienteId(Long clienteId) {
        log.debug("[REPO-CARTAO] Listando cartões do cliente ID={}", clienteId);
        return this.repository.findByCliente_Id(clienteId).stream()
                .map(JpaCartaoClienteMapper::toEntity)
                .toList();
    }
}
