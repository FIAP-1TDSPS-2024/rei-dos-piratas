package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.domain.repository.UsuarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.JpaClienteMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.PageMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import org.springframework.data.domain.Pageable;

public class ClienteRepository implements UsuarioRepository{

    private final JpaClienteEntityRepository repository;

    public ClienteRepository(JpaClienteEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Usuario> listAll(int pageNumber, int pageSize) {
        return PageMapper.fromFrameworkPage(
                this.repository.findAll(
                        Pageable
                                .ofSize(pageSize)
                                .withPage(pageNumber)
                ).map(JpaClienteMapper::toEntity));
    }

    @Override
    public Usuario findById(Long id) {
        return JpaClienteMapper.toEntity(
                this.repository
                        .findById(id)
                        .orElseThrow());
    }

    @Override
    public Usuario create(Usuario usuario) {
        return null;
    }

    @Override
    public Usuario update(Usuario usuario) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
