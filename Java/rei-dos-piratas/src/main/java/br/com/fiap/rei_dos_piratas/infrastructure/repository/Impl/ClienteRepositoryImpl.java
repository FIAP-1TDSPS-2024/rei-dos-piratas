package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaEnderecoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import org.springframework.data.domain.Pageable;

public class ClienteRepositoryImpl implements ClienteRepository {

    private final JpaClienteEntityRepository repository;

    public ClienteRepositoryImpl(JpaClienteEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Cliente> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaClienteMapper::toEntity));
    }

    @Override
    public Cliente findById(Long id) {
        return JpaClienteMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Cliente create(Cliente cliente) {

        //Verificação para evitar userNames, e-mail ou cpfs duplicados em banco
        if (this.repository.findFirstByUserName(cliente.getUserName()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if (this.repository.findFirstByCpf(cliente.getCpf()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse CPF. Insira um novo CPF válido");
        }
        if(this.repository.findFirstByEmail(cliente.getEmail()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse email. Insira um novo e-mail válido");
        }

        //Consulta por cidade e estado existentes para evitar duplicaçãp
        JpaEnderecoEntity jpaEndereco = JpaEnderecoMapper.toJpaEntity(cliente.getEndereco());
        JpaClienteEntity jpaCidadeExistente = this.repository.findFirstByEndereco_Cidade_CidadeNome(cliente.getEndereco().getCidade());

        if (jpaCidadeExistente != null) {
            if (jpaEndereco.getCidade().getCidadeNome().equals(jpaCidadeExistente.getEndereco().getCidade().getCidadeNome())) {
                jpaEndereco.setCidade(jpaCidadeExistente.getEndereco().getCidade());
            }
        }
        else{
            JpaClienteEntity jpaEstadoExistente = this.repository.findFirstByEndereco_Cidade_Estado_EstadoNome(cliente.getEndereco().getEstadoNome());

            if (jpaEstadoExistente != null) {
                if (jpaEndereco.getCidade().getEstado().getEstadoNome().equals(jpaEstadoExistente.getEndereco().getCidade().getEstado().getEstadoNome())) {
                    jpaEndereco.getCidade().setEstado(jpaEstadoExistente.getEndereco().getCidade().getEstado());
                }
            }
        }

        return JpaClienteMapper.toEntity(
                this.repository
                        .save(JpaClienteMapper.toJpaEntity(cliente, jpaEndereco)));
    }

    @Override
    public Cliente update(Cliente cliente) {

        JpaEnderecoEntity jpaEndereco = JpaEnderecoMapper.toJpaEntity(cliente.getEndereco());

        return JpaClienteMapper.toEntity(
                this.repository
                        .save(JpaClienteMapper.toJpaEntity(cliente, jpaEndereco)));
    }

    @Override
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}
