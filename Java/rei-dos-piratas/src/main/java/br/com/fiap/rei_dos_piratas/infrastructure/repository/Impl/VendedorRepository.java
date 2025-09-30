package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.domain.repository.UsuarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;

public class VendedorRepository implements UsuarioRepository {

    private final JpaVendedorEntityRepository repository;

    public VendedorRepository(JpaVendedorEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Usuario> listAll(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public Usuario findById(Long id) {
        return null;
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
