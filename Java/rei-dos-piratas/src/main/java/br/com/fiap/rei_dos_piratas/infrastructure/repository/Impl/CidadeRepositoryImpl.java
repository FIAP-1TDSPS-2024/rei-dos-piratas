package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.endereco.JpaCidadeMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCidadeEntityRepository;

public class CidadeRepositoryImpl implements CidadeRepository {

    private final JpaCidadeEntityRepository repository;

    public CidadeRepositoryImpl(JpaCidadeEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cidade save(Cidade cidade) {
        return JpaCidadeMapper
                .toEntity(this.repository
                        .save(JpaCidadeMapper.toJpaEntity(cidade)));
    }

    @Override
    public Cidade findById(Long id) {
        return JpaCidadeMapper
                .toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    public Cidade findFirstByCidadeNomeAndEstadoNome(String cidadeNome, String estadoNome) {
        return JpaCidadeMapper
                .toEntity(this.repository.findFirstByCidadeNomeIgnoreCaseAndEstado_EstadoNomeIgnoreCase(cidadeNome, estadoNome));
    }
}
