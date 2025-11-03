package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaFuncionarioMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class FuncionarioRepositoryImpl implements FuncionarioRepository {

    private final JpaFuncionarioEntityRepository repository;

    public FuncionarioRepositoryImpl(JpaFuncionarioEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Funcionario> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                Pageable
                        .ofSize(pageSize)
                        .withPage(pageNumber)
        ).map(JpaFuncionarioMapper::toEntity));
    }

    @Override
    public Funcionario findById(Long id) {
        return JpaFuncionarioMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Funcionario create(Funcionario funcionario) {

        //Verificação para evitar e-mail ou userNames duplicados em banco
        if (this.repository.findFirstByUserName(funcionario.getUsername()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um funcionario com esse nome de usuário. Insira um novo nome de usuário válido");
        }
        if(this.repository.findFirstByEmail(funcionario.getEmail()) != null) {
            throw new UniqueKeyDuplicatedException("Já existe um funcionario registrado com esse email. Insira um novo e-mail válido");
        }

        return JpaFuncionarioMapper.toEntity(
                this.repository.save(
                        JpaFuncionarioMapper.toJpaEntity(funcionario)));
    }

    @Override
    public Funcionario update(Funcionario funcionario) {

        Optional<JpaFuncionarioEntity> vendedorExistente = this.repository.findById(funcionario.getId());

        if (vendedorExistente.isPresent()) {
            return JpaFuncionarioMapper.toEntity(
                    this.repository.save(
                            JpaFuncionarioMapper.toJpaEntity(funcionario)));
        }
        else{
            return null;
        }
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Funcionario findByUsername(String username) {
        JpaFuncionarioEntity funcionario = this.repository.findFirstByUserName(username);
        if (funcionario != null) {
            return JpaFuncionarioMapper.toEntity(funcionario);
        }
        else {
            return null;
        }
    }
}
