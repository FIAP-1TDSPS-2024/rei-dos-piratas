package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

public interface ProdutoService {
    public Produto findById(Long id);
    public Page<Produto> findAll(int pageNumber, int PageSize);
    public Produto create(Produto produto);
    public Produto update(Produto produto);
    public void delete(Long id);
}
