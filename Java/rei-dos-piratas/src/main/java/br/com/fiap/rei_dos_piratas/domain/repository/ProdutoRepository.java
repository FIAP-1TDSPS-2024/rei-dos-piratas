package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

public interface ProdutoRepository {
    Page<Produto> listAll(int pageNumber, int pageSize);
    Produto findById(Long id);
    Produto create(Produto produto);
    Produto update(Produto produto);
    void delete(Long id);
}
