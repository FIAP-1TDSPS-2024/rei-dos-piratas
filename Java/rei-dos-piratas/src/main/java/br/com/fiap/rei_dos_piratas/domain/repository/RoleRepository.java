package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Role;

public interface RoleRepository {
    Page<Role> listAll(int pageNumber, int pageSize);
    Role findById(Long id);
    Role findByNome(String nome);
    Role findByIdWithPerfis(Long id);
    Role findByNomeWithPerfis(String nome);
}
