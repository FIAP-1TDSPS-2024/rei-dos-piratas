package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaProdutoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaEnderecoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEnderecoEntityRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnderecoRepositoryImpl implements EnderecoRepository {

    private final JpaEnderecoEntityRepository repository;

    public EnderecoRepositoryImpl(JpaEnderecoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Endereco> findAllByClienteId(Long clienteId, int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaEnderecoMapper::toEntity));
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
    public Endereco update(Endereco endereco) {
        Optional<JpaEnderecoEntity> enderecoExistente = this.repository.findById(endereco.getId());

        if (enderecoExistente.isPresent()) {
            return JpaEnderecoMapper.toEntity(
                    this.repository.save(
                            JpaEnderecoMapper.toJpaEntity(endereco)));
        }
        else{
            return null;
        }
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
    public Endereco getEnderecoEmpresa() {
        return JpaEnderecoMapper
                .toEntity(this.repository
                        .findFirstByEmpresa_Id(1L));
    }

    @Override
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}
