package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.UsuarioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepository;

public class ClienteService implements UsuarioService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
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
