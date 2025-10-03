package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.UsuarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepository;

public class VendedorService implements UsuarioService {

    private final VendedorRepository repository;

    public VendedorService(VendedorRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Usuario> listAll(int pageNumber, int pageSize) {
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Override
    public Usuario findById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public Usuario create(Usuario usuario) {
        return this.repository.create(usuario);
    }

    @Override
    public Usuario update(Usuario usuario) {
        return this.repository.update(usuario);
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
