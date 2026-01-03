package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaEnderecoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEnderecoEntityRepository;

import java.util.List;
import java.util.stream.Collectors;

public class EnderecoRepositoryImpl implements EnderecoRepository {

    private final JpaEnderecoEntityRepository repository;

    public EnderecoRepositoryImpl(JpaEnderecoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Endereco> findAllByClienteId(Long clienteId) {
        return this.repository
                .findAllByCliente_Id(clienteId)
                .stream()
                .map(JpaEnderecoMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Endereco findById(Long id) {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .findById(id).orElseThrow());
    }

    @Override
    public Endereco save(Endereco endereco) {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .save(JpaEnderecoMapper.toJpaEntity(endereco)));
    }

    @Override
    public Endereco findFirstByCidade(String cidade) {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .findFirstByCidade_CidadeNome(cidade));
    }

    @Override
    public Endereco findFirstByEstado(String estado) {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .findFirstByCidade_Estado_EstadoNome(estado));
    }

    @Override
    public Endereco getDadosEmpresa() {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .findFirstByEmpresa_Id(1L));
    }
}
