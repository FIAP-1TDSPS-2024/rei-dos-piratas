package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final Validator validator;

    public ClienteServiceImpl(ClienteRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository, Validator validator) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.validator = validator;
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

        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        log.info("[CLIENTE] Atualizando dados do cliente ID={}", userDetails.getId());

        Cliente cliente = this.findById(userDetails.getId());

        cliente.setUserName(updCliente.getUsername());
        cliente.setNomeCompleto(updCliente.getNomeCompleto());
        cliente.setEmail(updCliente.getEmail());
        boolean atualizarSenha = updCliente.getPassword() != null && !updCliente.getPassword().isBlank();
        if (atualizarSenha) {
            log.debug("[CLIENTE] Atualização de senha solicitada para cliente ID={}", userDetails.getId());
            cliente.setSenha(updCliente.getSenha());
        }
        cliente.setDataNascimento(updCliente.getDataNascimento());
        cliente.setSexo(updCliente.getSexo());
        cliente.setCpf(updCliente.getCpf());
        cliente.setCelular(updCliente.getCelular());

        if (atualizarSenha) {
            validar(cliente);
            String encryptedPassword = this.passwordEncoder.encode(updCliente.getPassword());
            cliente.setSenha(encryptedPassword);
        } else {
            validarExcetoSenha(cliente);
        }

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
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        log.info("[CLIENTE] Desativando conta do cliente ID={}", userDetails.getId());
        this.repository.delete(userDetails.getId());
        log.info("[CLIENTE] Conta do cliente ID={} desativada com sucesso", userDetails.getId());
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
