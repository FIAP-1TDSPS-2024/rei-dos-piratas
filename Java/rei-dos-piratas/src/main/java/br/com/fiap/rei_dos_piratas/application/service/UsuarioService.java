package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;

public interface UsuarioService {
    Page<Usuario> listAll(int pageNumber, int pageSize);

    Usuario findById(Long id);

    Usuario create(Usuario usuario);

    Usuario update(Usuario usuario);

    void delete(Long id);
}
