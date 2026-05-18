package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaProdutoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaProdutoEntityRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class ProdutoRepositoryImpl implements ProdutoRepository {

    private final JpaProdutoEntityRepository repository;

    public ProdutoRepositoryImpl(JpaProdutoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Produto> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaProdutoMapper::toEntity));
    }


    @Override
    public Produto findById(Long id) {
        return JpaProdutoMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Produto create(Produto produto) {
        return JpaProdutoMapper.toEntity(
                this.repository.save(
                        JpaProdutoMapper.toJpaEntity(produto)));
    }

    @Override
    public Produto update(Produto produto) {
        Optional<JpaProdutoEntity> produtoExistente = this.repository.findById(produto.getId());

        if (produtoExistente.isPresent()) {
            return JpaProdutoMapper.toEntity(
                    this.repository.save(
                            JpaProdutoMapper.toJpaEntity(produto)));
        }
        else{
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}
