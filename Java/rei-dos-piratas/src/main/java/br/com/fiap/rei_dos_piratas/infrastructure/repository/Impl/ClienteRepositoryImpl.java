package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaPerfilEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaCarrinhoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaPerfilMapper;
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

        JpaPerfilEntity jpaPerfil = JpaPerfilMapper.toJpaEntity(cliente.getPerfil());

        return JpaClienteMapper.toEntity(
                this.repository
                        .save(JpaClienteMapper.toJpaEntity(cliente, jpaCarrinho, jpaPerfil)));
    }

    @Override
    public Cliente update(Cliente updCliente) {
        Optional<JpaClienteEntity> clienteExistenteOpt = this.repository.findById(updCliente.getId());

        if (clienteExistenteOpt.isPresent()) {
            JpaClienteEntity entidadeGerenciada = clienteExistenteOpt.get();

            // Validação de unicidade excluindo o próprio usuário
            JpaClienteEntity porUserName = this.repository.findFirstByUserName(updCliente.getUsername());
            if (porUserName != null && !porUserName.getId().equals(entidadeGerenciada.getId())) {
                throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse nome de usuário. Insira um novo nome de usuário válido");
            }

            JpaClienteEntity porEmail = this.repository.findFirstByEmail(updCliente.getEmail());
            if (porEmail != null && !porEmail.getId().equals(entidadeGerenciada.getId())) {
                throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse email. Insira um novo e-mail válido");
            }

            JpaClienteEntity porCpf = this.repository.findFirstByCpf(updCliente.getCpf());
            if (porCpf != null && !porCpf.getId().equals(entidadeGerenciada.getId())) {
                throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse CPF. Insira um novo CPF válido");
            }

            // Atualiza apenas campos editáveis na entidade gerenciada.
            // Não é necessário chamar save() — como a entidade é managed dentro da transação
            // @Transactional do service, o Hibernate faz o flush automaticamente no commit.
            entidadeGerenciada.setUserName(updCliente.getUsername());
            entidadeGerenciada.setNomeCompleto(updCliente.getNomeCompleto());
            entidadeGerenciada.setEmail(updCliente.getEmail());
            entidadeGerenciada.setSenha(updCliente.getSenha());
            entidadeGerenciada.setUsuarioAtivo(updCliente.isUsuarioAtivo());
            entidadeGerenciada.setDataNascimento(updCliente.getDataNascimento());
            entidadeGerenciada.setSexo(updCliente.getSexo());
            entidadeGerenciada.setCpf(updCliente.getCpf());
            entidadeGerenciada.setCelular(updCliente.getCelular());

            return JpaClienteMapper.toEntity(entidadeGerenciada);
        }
        else {
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        Optional<JpaClienteEntity> clienteExistenteOpt = this.repository.findById(id);

        if (clienteExistenteOpt.isPresent()) {
            JpaClienteEntity entidadeGerenciada = clienteExistenteOpt.get();
            entidadeGerenciada.setUsuarioAtivo(false);
            this.repository.save(entidadeGerenciada);
        }
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

    @Override
    public Cliente findByEmail(String email) {
        JpaClienteEntity cliente = this.repository.findFirstByEmail(email);
        if (cliente != null) {
            return JpaClienteMapper.toEntity(cliente);
        } else {
            return null;
        }
    }
}
