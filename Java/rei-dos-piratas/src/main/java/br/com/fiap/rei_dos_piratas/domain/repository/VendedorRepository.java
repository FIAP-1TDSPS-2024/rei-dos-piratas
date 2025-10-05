package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;

public interface VendedorRepository{
    Page<Vendedor> listAll(int pageNumber, int pageSize);
    Vendedor findById(Long id);
    Vendedor create(Vendedor vendedor);
    Vendedor update(Vendedor vendedor);
    void delete(Long id);
}