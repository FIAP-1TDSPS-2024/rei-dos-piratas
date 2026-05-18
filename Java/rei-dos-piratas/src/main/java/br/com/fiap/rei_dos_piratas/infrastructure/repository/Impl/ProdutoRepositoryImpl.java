package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaProdutoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaProdutoEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
public class ProdutoRepositoryImpl implements ProdutoRepository {

    private final JpaProdutoEntityRepository repository;

    public ProdutoRepositoryImpl(JpaProdutoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Produto> listAll(int pageNumber, int pageSize) {
        log.debug("[REPO-PRODUTO] Listando todos os produtos - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaProdutoMapper::toEntity));
    }


    @Override
    public Produto findById(Long id) {
        log.debug("[REPO-PRODUTO] findById - ID={}", id);
        return JpaProdutoMapper.toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    public Produto create(Produto produto) {
        log.debug("[REPO-PRODUTO] Persistindo novo produto: nome='{}'", produto.getNome());
        Produto criado = JpaProdutoMapper.toEntity(
                this.repository.save(JpaProdutoMapper.toJpaEntity(produto)));
        log.info("[REPO-PRODUTO] Produto criado com sucesso - ID={}, nome='{}'", criado.getId(), criado.getNome());
        return criado;
    }

    @Override
    public Produto update(Produto produto) {
        log.debug("[REPO-PRODUTO] Atualizando produto ID={}", produto.getId());
        Optional<JpaProdutoEntity> produtoExistente = this.repository.findById(produto.getId());

        if (produtoExistente.isPresent()) {
            Produto atualizado = JpaProdutoMapper.toEntity(
                    this.repository.save(JpaProdutoMapper.toJpaEntity(produto)));
            log.debug("[REPO-PRODUTO] Produto ID={} atualizado com sucesso", produto.getId());
            return atualizado;
        } else {
            log.warn("[REPO-PRODUTO] Produto ID={} não encontrado para atualização", produto.getId());
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        log.info("[REPO-PRODUTO] Removendo produto ID={}", id);
        this.repository.deleteById(id);
        log.info("[REPO-PRODUTO] Produto ID={} removido com sucesso", id);
    }
}
