package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Usuario;

public interface ClienteRepository {
    Page<Cliente> listAll(int pageNumber, int pageSize);
    Cliente findById(Long id);
    Cliente create(Cliente cliente);
    Cliente update(Cliente cliente);
    void delete(Long id);
}
