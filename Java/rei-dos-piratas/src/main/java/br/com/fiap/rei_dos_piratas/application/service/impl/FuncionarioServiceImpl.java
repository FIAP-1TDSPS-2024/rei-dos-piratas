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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
        log.debug("[FUNCIONARIO] Listando funcionários ativos - página: {}, tamanho: {}", pageNumber, pageSize);
        return this.repository.findAllByUsuarioAtivoTrue(pageNumber, pageSize);
    }

    @Override
    public Funcionario findById(Long id) {
        log.debug("[FUNCIONARIO] Buscando funcionário por ID={}", id);
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            log.warn("[FUNCIONARIO] Funcionário não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um vendedor com o id " + id);
        }
    }

    @Override
    @Transactional
    public Funcionario create(Funcionario funcionario) {
        log.info("[FUNCIONARIO] Criando novo funcionário - username='{}', email='{}', perfil='{}'",
                funcionario.getUsername(), funcionario.getEmail(),
                funcionario.getPerfil() != null ? funcionario.getPerfil().getNome() : "N/A");
        validar(funcionario);
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);
        Perfil perfil = this.perfilRepository.findByNome(funcionario.getPerfil().getNome());
        funcionario.setPerfil(perfil);
        Funcionario funcionarioCriado = this.repository.create(funcionario);
        log.info("[FUNCIONARIO] Funcionário criado com sucesso - ID={}, username='{}'",
                funcionarioCriado.getId(), funcionarioCriado.getUsername());
        return funcionarioCriado;
    }

    @Override
    @Transactional
    public Funcionario update(Funcionario funcionario) {
        log.info("[FUNCIONARIO] Atualizando funcionário ID={}", funcionario.getId());
        validar(funcionario);
        String encryptedPassword = this.passwordEncoder.encode(funcionario.getPassword());
        funcionario.setSenha(encryptedPassword);
        Funcionario funcionarioAtualizado = this.repository.update(funcionario);
        if (funcionarioAtualizado == null) {
            log.error("[FUNCIONARIO] Falha ao atualizar funcionário ID={} - registro não encontrado", funcionario.getId());
            throw new ResourceNotFoundException("Não foi possível encontrar um funcionário com o id " + funcionario.getId() + ". Crie um novo funcionário.");
        }
        log.info("[FUNCIONARIO] Funcionário ID={} atualizado com sucesso", funcionarioAtualizado.getId());
        return funcionarioAtualizado;
    }

    @Override
    public Funcionario ativarDesativar(Long id) {
        Funcionario funcionario = this.findById(id);
        boolean novoStatus = !funcionario.isUsuarioAtivo();
        log.info("[FUNCIONARIO] Alterando status do funcionário ID={} para {} (era {})",
                id, novoStatus ? "ATIVO" : "INATIVO", funcionario.isUsuarioAtivo() ? "ATIVO" : "INATIVO");
        funcionario.setUsuarioAtivo(novoStatus);
        Funcionario atualizado = this.repository.update(funcionario);
        log.info("[FUNCIONARIO] Status do funcionário ID={} alterado com sucesso", id);
        return atualizado;
    }

    @Override
    public Funcionario findByEmail(String email) {
        log.debug("[FUNCIONARIO] Buscando funcionário por email='{}'", email);
        Funcionario funcionario = this.repository.findByEmail(email);
        if (funcionario == null) {
            log.warn("[FUNCIONARIO] Funcionário não encontrado: email='{}'", email);
            throw new ResourceNotFoundException("Não foi possível encontrar um funcionário com o e-mail " + email);
        }
        return funcionario;
    }

    private void validar(Funcionario funcionario) {
        Set<ConstraintViolation<Funcionario>> violacoes = validator.validate(funcionario);
        if (!violacoes.isEmpty()) {
            log.warn("[FUNCIONARIO] Validação falhou para funcionário username='{}' - {} violação(ões)",
                    funcionario.getUsername(), violacoes.size());
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
