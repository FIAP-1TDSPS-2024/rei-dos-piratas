package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;

public interface VendedorService {
    Page<Vendedor> listAll(int pageNumber, int pageSize);

    Vendedor findById(Long id);

    Vendedor create(Vendedor vendedor);

    Vendedor update(Vendedor vendedor);

    void delete(Long id);
}
