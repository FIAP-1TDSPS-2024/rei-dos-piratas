package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;

public interface ClienteService {
    Page<Cliente> listAll(int pageNumber, int pageSize);

    Cliente findById(Long id);

    Cliente findByUsername(String username);

    Cliente findByEmail(String email);

    Cliente create(Cliente cliente);

    Cliente update(Cliente cliente);

    void delete();
}
