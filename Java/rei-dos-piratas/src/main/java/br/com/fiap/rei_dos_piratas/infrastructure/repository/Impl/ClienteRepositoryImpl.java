package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCarrinhoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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
        if (this.repository.findFirstByUserName(cliente.getUsername()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if (this.repository.findFirstByCpf(cliente.getCpf()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse CPF. Insira um novo CPF válido");
        }
        if(this.repository.findFirstByEmail(cliente.getEmail()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse email. Insira um novo e-mail válido");
        }

        JpaCarrinhoEntity jpaCarrinho = JpaCarrinhoMapper.toJpaEntity(cliente.getCarrinho());

        return JpaClienteMapper.toEntity(
                this.repository
                        .save(JpaClienteMapper.toJpaEntity(cliente, jpaCarrinho)));
    }

    @Override
    public Cliente update(Cliente cliente) {

        Optional<JpaClienteEntity> clienteExistente = this.repository.findById(cliente.getId());

        if (clienteExistente.isPresent()) {
            JpaCarrinhoEntity jpaCarrinho = JpaCarrinhoMapper.toJpaEntity(cliente.getCarrinho());

            return JpaClienteMapper.toEntity(
                    this.repository
                            .save(JpaClienteMapper.toJpaEntity(cliente, jpaCarrinho)));
        }
        else {
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        this.repository.deleteById(id);
    }

    @Override
    public Cliente findByUsername(String username) {

        JpaClienteEntity cliente = this.repository.findFirstByUserName(username);

        if (cliente != null) {
            return JpaClienteMapper.toEntity(cliente);
        } else {
            return null;
        }
    }
}
