package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaEstadoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEstadoEntityRepository;

public class EstadoRepositoryImpl implements EstadoRepository {

    private final JpaEstadoEntityRepository repository;

    public EstadoRepositoryImpl(JpaEstadoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Estado save(Estado estado) {
        return JpaEstadoMapper
                .toEntity(this.repository
                        .save(JpaEstadoMapper.toJpaEntity(estado)));
    }

    @Override
    public Estado findById(Long id) {
        return JpaEstadoMapper
                .toEntity(this.repository.findById(id).orElseThrow());
    }

    @Override
    public Estado findFirstByNome(String nome) {
        return JpaEstadoMapper
                .toEntity(this.repository.findFirstByEstadoNomeIgnoreCase(nome));
    }
}
