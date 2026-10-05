package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import br.com.fiap.rei_dos_piratas.domain.entity.Sessao;
import br.com.fiap.rei_dos_piratas.domain.repository.SessaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaSessaoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SessaoRepositoryImpl implements SessaoRepository {
    private final EntityManager em;
    public SessaoRepositoryImpl(EntityManager em) { this.em = em; }

    @Transactional(readOnly = true)
    public Optional<Sessao> findById(UUID id) {
        return Optional.ofNullable(em.find(JpaSessaoEntity.class, id)).map(entity -> toDomain(entity, false));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Sessao> findByIdParaAtualizar(UUID id) {
        JpaSessaoEntity entity = em.find(JpaSessaoEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) return Optional.empty();
        // Atualiza tambem se a entidade ja foi carregada pelo filtro nesta requisicao.
        em.refresh(entity, LockModeType.PESSIMISTIC_WRITE);
        return Optional.of(toDomain(entity, true));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void salvar(Sessao sessao) {
        JpaSessaoEntity entity = em.find(JpaSessaoEntity.class, sessao.getId());
        boolean nova = entity == null;
        if (nova) entity = new JpaSessaoEntity();
        entity.setId(sessao.getId());
        entity.setTipo(sessao.getIdentidade().tipo());
        entity.setUsuarioId(sessao.getIdentidade().id());
        entity.setCriadaEm(sessao.getCriadaEm());
        entity.setExpiraEm(sessao.getExpiraEm());
        entity.setRevogadaEm(sessao.getRevogadaEm());
        entity.setRefreshHash(sessao.getRefreshHash());
        entity.getRefreshUsados().clear();
        entity.getRefreshUsados().addAll(sessao.getRefreshUsados());
        if (nova) em.persist(entity);
    }

    private Sessao toDomain(JpaSessaoEntity entity, boolean carregarHistorico) {
        return new Sessao(entity.getId(), new IdentidadeConta(entity.getTipo(), entity.getUsuarioId()),
                entity.getCriadaEm(), entity.getExpiraEm(), entity.getRevogadaEm(),
                entity.getRefreshHash(), carregarHistorico ? entity.getRefreshUsados() : java.util.Set.of());
    }
}
