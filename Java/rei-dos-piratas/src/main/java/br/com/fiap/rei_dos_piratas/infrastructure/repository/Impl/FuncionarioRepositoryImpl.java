package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaFuncionarioMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

    private final JpaFuncionarioEntityRepository repository;

    public FuncionarioRepositoryImpl(JpaFuncionarioEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Funcionario> listAll(int pageNumber, int pageSize) {
        log.debug("[REPO-FUNCIONARIO] Listando todos os funcionários - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(Pageable.ofSize(pageSize).withPage(pageNumber))
                        .map(JpaFuncionarioMapper::toEntity));
    }

    @Override
    public Funcionario findById(Long id) {
        log.debug("[REPO-FUNCIONARIO] findById - ID={}", id);
        return JpaFuncionarioMapper.toEntity(
                this.repository.findById(id).orElseThrow());
    }

    @Override
    public Funcionario create(Funcionario funcionario) {
        log.debug("[REPO-FUNCIONARIO] Verificando duplicidade antes de criar funcionário - username='{}', email='{}'",
                funcionario.getUsername(), funcionario.getEmail());

        //Verificação para evitar e-mail ou userNames duplicados em banco
        if (this.repository.findFirstByUserName(funcionario.getUsername()) != null) {
            log.warn("[REPO-FUNCIONARIO] Username duplicado: '{}'", funcionario.getUsername());
            throw new UniqueKeyDuplicatedException("Já existe um funcionario com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if (this.repository.findFirstByEmail(funcionario.getEmail()) != null) {
            log.warn("[REPO-FUNCIONARIO] Email duplicado: '{}'", funcionario.getEmail());
            throw new UniqueKeyDuplicatedException("Já existe um funcionario registrado com esse email. Insira um novo e-mail válido");
        }

        Funcionario funcionarioCriado = JpaFuncionarioMapper.toEntity(
                this.repository.save(JpaFuncionarioMapper.toJpaEntity(funcionario)));
        log.info("[REPO-FUNCIONARIO] Funcionário criado com sucesso - ID={}", funcionarioCriado.getId());
        return funcionarioCriado;
    }

    @Override
    public Funcionario update(Funcionario funcionario) {
        log.debug("[REPO-FUNCIONARIO] Atualizando funcionário ID={}", funcionario.getId());
        Optional<JpaFuncionarioEntity> vendedorExistente = this.repository.findById(funcionario.getId());

        if (vendedorExistente.isPresent()) {
            Funcionario atualizado = JpaFuncionarioMapper.toEntity(
                    this.repository.save(JpaFuncionarioMapper.toJpaEntity(funcionario)));
            log.debug("[REPO-FUNCIONARIO] Funcionário ID={} salvo com sucesso", funcionario.getId());
            return atualizado;
        } else {
            log.warn("[REPO-FUNCIONARIO] Funcionário ID={} não encontrado para atualização", funcionario.getId());
            return null;
        }
    }

    @Override
    public Page<Funcionario> findAllByUsuarioAtivoTrue(int pageNumber, int pageSize) {
        log.debug("[REPO-FUNCIONARIO] Listando funcionários ativos - página: {}, tamanho: {}", pageNumber, pageSize);
        return PageMapper.fromFrameworkPage(
                this.repository.findAllByUsuarioAtivoTrue(
                        Pageable.ofSize(pageSize).withPage(pageNumber)
                ).map(JpaFuncionarioMapper::toEntity));
    }

    @Override
    public Funcionario findByUsername(String username) {
        log.debug("[REPO-FUNCIONARIO] findByUsername - username='{}'", username);
        JpaFuncionarioEntity funcionario = this.repository.findFirstByUserName(username);
        if (funcionario != null) {
            return JpaFuncionarioMapper.toEntity(funcionario);
        } else {
            log.debug("[REPO-FUNCIONARIO] Nenhum funcionário encontrado para username='{}'", username);
            return null;
        }
    }

    @Override
    public Funcionario findByEmail(String email) {
        log.debug("[REPO-FUNCIONARIO] findByEmail - email='{}'", email);
        return this.repository.findByEmailWithRoles(email)
                .map(JpaFuncionarioMapper::toEntityWithRoles)
                .orElse(null);
    }
}
