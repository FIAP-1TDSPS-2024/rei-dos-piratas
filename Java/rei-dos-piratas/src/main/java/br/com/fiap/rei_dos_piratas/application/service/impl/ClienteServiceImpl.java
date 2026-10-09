package br.com.fiap.rei_dos_piratas.application.service.impl;


import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.ClienteCobrancaRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.ClienteCobrancaResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final SenhaService passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final Validator validator;
    private final ContaRepository contas;
    private final UsuarioAtualService usuarioAtual;
    private final CobrancaAppClient apiCobranca;

    public ClienteServiceImpl(ClienteRepository repository, SenhaService passwordEncoder, PerfilRepository perfilRepository, Validator validator, ContaRepository contas, UsuarioAtualService usuarioAtual, CobrancaAppClient apiCobranca) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.validator = validator;
        this.contas = contas;
        this.usuarioAtual = usuarioAtual;
        this.apiCobranca = apiCobranca;
    }

    @Override
    public Page<Cliente> listAll(int pageNumber, int pageSize) {
        log.debug("[CLIENTE] Listando todos os clientes - página: {}, tamanho: {}", pageNumber, pageSize);
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Cliente findById(Long id) {
        log.debug("[CLIENTE] Buscando cliente por ID={}", id);
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            log.warn("[CLIENTE] Cliente não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + id);
        }
    }

    public Cliente findByUsername(String username) {
        log.debug("[CLIENTE] Buscando cliente por username='{}'", username);
        try {
            return this.repository.findByUsername(username);
        } catch (NoSuchElementException e) {
            log.warn("[CLIENTE] Cliente não encontrado: username='{}'", username);
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o username " + username);
        }
    }

    @Override
    public Cliente findByEmail(String email) {
        log.debug("[CLIENTE] Buscando cliente por email='{}'", email);
        try {
            return this.repository.findByEmail(email);
        } catch (NoSuchElementException e) {
            log.warn("[CLIENTE] Cliente não encontrado: email='{}'", email);
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o e-mail " + email);
        }
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        log.info("[CLIENTE] Criando novo cliente - username='{}', email='{}'", cliente.getUsername(), cliente.getEmail());
        validar(cliente);
        validarEmail(cliente.getEmail(), null);
        String encryptedPassword = this.passwordEncoder.encode(cliente.getPassword());
        cliente.setSenha(encryptedPassword);
        Perfil perfil = this.perfilRepository.findByNomeWithRoles("CLIENT");
        cliente.setPerfil(perfil);
        Cliente clienteCriado = this.repository.create(cliente);
        log.info("[CLIENTE] Cliente criado com sucesso - ID={}, username='{}'", clienteCriado.getId(), clienteCriado.getUsername());
        return clienteCriado;
    }

    @Override
    @Transactional
    public Cliente update(Cliente updCliente) {

        IdentidadeConta identidade = identidadeCliente();

        log.info("[CLIENTE] Atualizando dados do cliente ID={}", identidade.id());

        Cliente cliente = this.findById(identidade.id());

        cliente.setUserName(updCliente.getUsername());
        cliente.setNomeCompleto(updCliente.getNomeCompleto());
        cliente.setEmail(updCliente.getEmail());
        cliente.setDataNascimento(updCliente.getDataNascimento());
        cliente.setSexo(updCliente.getSexo());
        cliente.setCpf(updCliente.getCpf());
        cliente.setCelular(updCliente.getCelular());
        if (updCliente.getIdCobranca() != null && !updCliente.getIdCobranca().isBlank()) {
            cliente.setIdCobranca(updCliente.getIdCobranca());
        }

        boolean atualizarSenha = false;

        if (updCliente.getPassword() != null && !updCliente.getPassword().isBlank()) {
            // Cliente carregado do banco já traz o hash: igual ao atual significa que não há troca
            boolean mesmoHash = updCliente.getPassword().equals(cliente.getSenha());
            atualizarSenha = !mesmoHash && !this.passwordEncoder.matches(updCliente.getPassword(), cliente.getSenha());
        }

        if (atualizarSenha) {
            log.debug("[CLIENTE] Atualização de senha solicitada para cliente ID={}", identidade.id());
            cliente.setSenha(updCliente.getSenha());
            validar(cliente);
            String encryptedPassword = this.passwordEncoder.encode(updCliente.getPassword());
            cliente.setSenha(encryptedPassword);
        } else {
            validarExcetoSenha(cliente);
        }

        validarEmail(cliente.getEmail(), identidade);
        Cliente clienteAtualizado = this.repository.update(cliente);

        if (clienteAtualizado == null) {
            log.error("[CLIENTE] Falha ao atualizar cliente ID={} - registro não encontrado no repositório", cliente.getId());
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + cliente.getId() + ". Crie um novo cliente.");
        }

        log.info("[CLIENTE] Cliente ID={} atualizado com sucesso", clienteAtualizado.getId());
        return clienteAtualizado;
    }

    @Override
    @Transactional
    public void delete() {
        IdentidadeConta identidade = identidadeCliente();

        log.info("[CLIENTE] Desativando conta do cliente ID={}", identidade.id());
        this.repository.delete(identidade.id());
        log.info("[CLIENTE] Conta do cliente ID={} desativada com sucesso", identidade.id());
    }

    @Override
    public Cliente obterOuCriarClienteCobranca(Cliente cliente, Endereco endereco) {
        log.info("[COBRANCA - CLIENTE] Verificando se há cliente de cobrança para o cliente ID={}", cliente.getId());

        if (cliente.getIdCobranca() != null && !cliente.getIdCobranca().isBlank()) {
            log.info("[COBRANCA - CLIENTE] Cliente de cobrança já existe para o cliente ID={}", cliente.getId());
            return cliente;
        }

        log.info("[COBRANCA - CLIENTE] Criando cliente de cobrança para o cliente ID={}", cliente.getId());
        ClienteCobrancaRequestDto request = definirClienteCobrancaRequest(cliente, endereco);
        ClienteCobrancaResponseDto response = this.apiCobranca.criarNovoCliente(request);

        log.info("[COBRANCA - CLIENTE] Criado cliente de cobrança com ID {} para o cliente ID={}", response.id(), cliente.getId());
        cliente.setIdCobranca(response.id());

        return this.update(cliente);
    }

    private ClienteCobrancaRequestDto definirClienteCobrancaRequest(Cliente cliente, Endereco endereco) {
        return new ClienteCobrancaRequestDto(
                cliente.getNomeCompleto(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getCelular(),
                endereco.getLogradouro(),
                String.valueOf(endereco.getNumero()),
                endereco.getBairro(),
                endereco.getCep(),
                cliente.getId().toString(),
                true
        );
    }

    private void validar(Cliente cliente) {
        Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);
        if (!violacoes.isEmpty()) {
            log.warn("[CLIENTE] Validação falhou para cliente username='{}' - {} violação(ões)", cliente.getUsername(), violacoes.size());
            Map<String, String> erros = violacoes.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (m1, m2) -> m1
                    ));
            throw new ValidacaoException(erros);
        }
    }

    private IdentidadeConta identidadeCliente() {
        IdentidadeConta identidade = usuarioAtual.identidade();
        if (identidade.tipo() != TipoConta.CLIENTE) {
            throw new br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException();
        }
        return identidade;
    }

    private void validarEmail(String email, IdentidadeConta propriaConta) {
        if (contas.emailOcupado(email, propriaConta)) {
            throw new UniqueKeyDuplicatedException("O e-mail ja pertence a uma conta.");
        }
    }

    private void validarExcetoSenha(Cliente cliente) {
        Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente).stream()
                .filter(v -> !"senha".equals(v.getPropertyPath().toString()))
                .collect(Collectors.toSet());
        if (!violacoes.isEmpty()) {
            log.warn("[CLIENTE] Validação (sem senha) falhou para cliente ID={} - {} violação(ões)", cliente.getId(), violacoes.size());
            Map<String, String> erros = violacoes.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (m1, m2) -> m1
                    ));
            throw new ValidacaoException(erros);
        }
    }
}
