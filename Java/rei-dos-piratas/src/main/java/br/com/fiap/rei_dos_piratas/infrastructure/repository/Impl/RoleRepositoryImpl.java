package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Role;
import br.com.fiap.rei_dos_piratas.domain.repository.RoleRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaRoleMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaRoleEntityRepository;
import org.springframework.data.domain.Pageable;

public class RoleRepositoryImpl implements RoleRepository {

    private final JpaRoleEntityRepository repository;

    public RoleRepositoryImpl(JpaRoleEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Role> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaRoleMapper::toEntity));
    }

    @Override
    public Role findById(Long id) {
        return JpaRoleMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }


    @Override
    public Role findByNome(String nome) {
        return JpaRoleMapper.toEntity(
                this.repository.findFirstByNomeIgnoreCase(nome));
    }
}
