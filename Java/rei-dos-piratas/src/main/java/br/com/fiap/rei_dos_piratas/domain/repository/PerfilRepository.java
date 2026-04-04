package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;

public interface PerfilRepository {
    Page<Perfil> listAll(int pageNumber, int pageSize);
    Perfil findById(Long id);
    Perfil findByNome(String nome);
    Perfil findByIdWithRoles(Long id);
    Perfil findByNomeWithRoles(String nome);
}
