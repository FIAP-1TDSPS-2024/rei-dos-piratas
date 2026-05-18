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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
public class ClienteRepositoryImpl implements ClienteRepository {

    private final JpaClienteEntityRepository repository;

    public ClienteRepositoryImpl(JpaClienteEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Cliente> listAll(int pageNumber, int pageSize) {
        log.debug("[REPO-CLIENTE] Listando todos os clientes - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable.ofSize(pageSize).withPage(pageNumber)
                ).map(JpaClienteMapper::toEntity));
    }

    @Override
    public Cliente findById(Long id) {
        log.debug("[REPO-CLIENTE] findById - ID={}", id);
        return JpaClienteMapper.toEntity(
                this.repository.findById(id).orElseThrow());
    }

    @Override
    public Cliente create(Cliente cliente) {
        log.debug("[REPO-CLIENTE] Verificando duplicidade antes de criar cliente - username='{}', email='{}'",
                cliente.getUsername(), cliente.getEmail());

        //Verificação para evitar userNames, e-mail ou cpfs duplicados em banco
        if (this.repository.findFirstByUserName(cliente.getUsername()) != null) {
            log.warn("[REPO-CLIENTE] Username duplicado: '{}'", cliente.getUsername());
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if (this.repository.findFirstByCpf(cliente.getCpf()) != null) {
            log.warn("[REPO-CLIENTE] CPF duplicado: '{}'", cliente.getCpf());
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse CPF. Insira um novo CPF válido");
        }
        if (this.repository.findFirstByEmail(cliente.getEmail()) != null) {
            log.warn("[REPO-CLIENTE] Email duplicado: '{}'", cliente.getEmail());
            throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse email. Insira um novo e-mail válido");
        }

        JpaCarrinhoEntity jpaCarrinho = JpaCarrinhoMapper.toJpaEntity(cliente.getCarrinho());
        JpaPerfilEntity jpaPerfil = JpaPerfilMapper.toJpaEntity(cliente.getPerfil());

        Cliente clienteSalvo = JpaClienteMapper.toEntity(
                this.repository.save(JpaClienteMapper.toJpaEntity(cliente, jpaCarrinho, jpaPerfil)));
        log.info("[REPO-CLIENTE] Cliente criado com sucesso - ID={}", clienteSalvo.getId());
        return clienteSalvo;
    }

    @Override
    public Cliente update(Cliente updCliente) {
        log.debug("[REPO-CLIENTE] Atualizando cliente ID={}", updCliente.getId());
        Optional<JpaClienteEntity> clienteExistenteOpt = this.repository.findById(updCliente.getId());

        if (clienteExistenteOpt.isPresent()) {
            JpaClienteEntity entidadeGerenciada = clienteExistenteOpt.get();

            // Validação de unicidade excluindo o próprio usuário
            JpaClienteEntity porUserName = this.repository.findFirstByUserName(updCliente.getUsername());
            if (porUserName != null && !porUserName.getId().equals(entidadeGerenciada.getId())) {
                log.warn("[REPO-CLIENTE] Username duplicado na atualização: '{}' já pertence ao ID={}", updCliente.getUsername(), porUserName.getId());
                throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse nome de usuário. Insira um novo nome de usuário válido");
            }

            JpaClienteEntity porEmail = this.repository.findFirstByEmail(updCliente.getEmail());
            if (porEmail != null && !porEmail.getId().equals(entidadeGerenciada.getId())) {
                log.warn("[REPO-CLIENTE] Email duplicado na atualização: '{}' já pertence ao ID={}", updCliente.getEmail(), porEmail.getId());
                throw new UniqueKeyDuplicatedException("Já existe um cliente registrado com esse email. Insira um novo e-mail válido");
            }

            JpaClienteEntity porCpf = this.repository.findFirstByCpf(updCliente.getCpf());
            if (porCpf != null && !porCpf.getId().equals(entidadeGerenciada.getId())) {
                log.warn("[REPO-CLIENTE] CPF duplicado na atualização: '{}' já pertence ao ID={}", updCliente.getCpf(), porCpf.getId());
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

            log.debug("[REPO-CLIENTE] Cliente ID={} atualizado (dirty-check Hibernate)", updCliente.getId());
            return JpaClienteMapper.toEntity(entidadeGerenciada);
        } else {
            log.warn("[REPO-CLIENTE] Cliente ID={} não encontrado para atualização", updCliente.getId());
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        log.info("[REPO-CLIENTE] Desativando cliente ID={}", id);
        Optional<JpaClienteEntity> clienteExistenteOpt = this.repository.findById(id);

        if (clienteExistenteOpt.isPresent()) {
            JpaClienteEntity entidadeGerenciada = clienteExistenteOpt.get();
            entidadeGerenciada.setUsuarioAtivo(false);
            this.repository.save(entidadeGerenciada);
            log.info("[REPO-CLIENTE] Cliente ID={} desativado com sucesso", id);
        } else {
            log.warn("[REPO-CLIENTE] Cliente ID={} não encontrado para desativação", id);
        }
    }

    @Override
    public Cliente findByUsername(String username) {
        log.debug("[REPO-CLIENTE] findByUsername - username='{}'", username);
        JpaClienteEntity cliente = this.repository.findFirstByUserName(username);
        if (cliente != null) {
            return JpaClienteMapper.toEntity(cliente);
        } else {
            log.debug("[REPO-CLIENTE] Nenhum cliente encontrado para username='{}'", username);
            return null;
        }
    }

    @Override
    public Cliente findByEmail(String email) {
        log.debug("[REPO-CLIENTE] findByEmail - email='{}'", email);
        JpaClienteEntity cliente = this.repository.findFirstByEmail(email);
        if (cliente != null) {
            return JpaClienteMapper.toEntity(cliente);
        } else {
            log.debug("[REPO-CLIENTE] Nenhum cliente encontrado para email='{}'", email);
            return null;
        }
    }
}
