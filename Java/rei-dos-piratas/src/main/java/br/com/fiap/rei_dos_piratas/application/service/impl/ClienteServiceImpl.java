package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Cliente findById(Long id) {
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + id);
        }
    }

    public Cliente findByUsername(String username) {
        try {
            return this.repository.findByUsername(username);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o username " + username);
        }
    }

    @Override
    public Cliente findByEmail(String email) {
        try {
            return this.repository.findByEmail(email);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o e-mail " + email);
        }
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        validar(cliente);
        String encryptedPassword = this.passwordEncoder.encode(cliente.getPassword());
        cliente.setSenha(encryptedPassword);
        Perfil perfil = this.perfilRepository.findByNomeWithRoles("CLIENT");
        cliente.setPerfil(perfil);
        return this.repository.create(cliente);
    }

    @Override
    @Transactional
    public Cliente update(Cliente cliente) {
        validar(cliente);
        String encryptedPassword = this.passwordEncoder.encode(cliente.getPassword());
        cliente.setSenha(encryptedPassword);
        Cliente clienteAtualizado = this.repository.update(cliente);
        if (clienteAtualizado == null) {
            throw new ResourceNotFoundException("Não foi possível encontrar um cliente com o id " + cliente.getId() + ". Crie um novo cliente.");
        }
        return clienteAtualizado;
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }

    private void validar(Cliente cliente) {
        Set<ConstraintViolation<Cliente>> violacoes = validator.validate(cliente);
        if (!violacoes.isEmpty()) {
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
