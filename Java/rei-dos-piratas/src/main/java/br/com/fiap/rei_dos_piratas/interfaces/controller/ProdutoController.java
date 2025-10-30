package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoOutDto;

public interface ProdutoController {
    public ProdutoOutDto findById(Long id);
    public Page<ProdutoOutDto> findAll(int pageNumber, int pageSize);
    public ProdutoOutDto create(ProdutoInDto produto);
    public ProdutoOutDto update(Produto produto);
    public void delete(Long id);
}
