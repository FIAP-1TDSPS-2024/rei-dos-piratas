package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class FuncionarioServiceImpl implements FuncionarioService {

    private final FuncionarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final Validator validator;

    public FuncionarioServiceImpl(FuncionarioRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository, Validator validator) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.validator = validator;
    }

    @Override
    public Page<Funcionario> listAll(int pageNumber, int pageSize) {
        return this.repository.findAllByUsuarioAtivoTrue(pageNumber, pageSize);
    }

    @Override
    public Funcionario findById(Long id) {
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Não foi possível encontrar um vendedor com o id " + id);
        }
    }

    @Override
    @Transactional
    public Funcionario create(Funcionario funcionario) {
        validar(funcionario);
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);
        Perfil perfil = this.perfilRepository.findByNome(funcionario.getPerfil().getNome());
        funcionario.setPerfil(perfil);
        return this.repository.create(funcionario);
    }

    @Override
    @Transactional
    public Funcionario update(Funcionario funcionario) {
        validar(funcionario);
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);
        Funcionario funcionarioAtualizado = this.repository.update(funcionario);
        if (funcionarioAtualizado == null) {
            throw new ResourceNotFoundException("Não foi possível encontrar um funcionário com o id " + funcionario.getId() + ". Crie um novo funcionário.");
        }
        return funcionarioAtualizado;
    }

    @Override
    public Funcionario ativarDesativar(Long id) {
        Funcionario funcionario = this.findById(id);
        funcionario.setUsuarioAtivo(!funcionario.isUsuarioAtivo());
        return this.repository.update(funcionario);
    }

    @Override
    public Funcionario findByEmail(String email) {
        Funcionario funcionario = this.repository.findByEmail(email);
        if (funcionario == null) {
            throw new ResourceNotFoundException("Não foi possível encontrar um funcionário com o e-mail " + email);
        }
        return funcionario;
    }

    private void validar(Funcionario funcionario) {
        Set<ConstraintViolation<Funcionario>> violacoes = validator.validate(funcionario);
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
