package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaPerfilMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPerfilEntityRepository;
import org.springframework.data.domain.Pageable;

public class PerfilRepositoryImpl implements PerfilRepository {

    private final JpaPerfilEntityRepository repository;

    public PerfilRepositoryImpl(JpaPerfilEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Perfil> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaPerfilMapper::toEntity));
    }

    @Override
    public Perfil findById(Long id) {
        return JpaPerfilMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }


    @Override
    public Perfil findByNome(String nome) {
        return JpaPerfilMapper.toEntity(
                this.repository.findFirstByNomeIgnoreCase(nome));
    }

    @Override
    public Perfil findByIdWithRoles(Long id) {
        return JpaPerfilMapper.toEntity(
                this.repository
                        .findByIdWithRoles(id)
                        .orElseThrow());
    }

    @Override
    public Perfil findByNomeWithRoles(String nome) {
        return JpaPerfilMapper.toEntity(
                this.repository
                        .findByNomeIgnoreCaseWithRoles(nome)
                        .orElseThrow());
    }
}
