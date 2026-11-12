package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UrlPreassinadaOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UploadImagemProdutoOutDto;

public interface ProdutoController {
    public ProdutoOutDto findById(Long id);
    public Page<ProdutoOutDto> findAll(int pageNumber, int pageSize);
    public ProdutoOutDto create(ProdutoInDto produto);
    public ProdutoOutDto update(Produto produto);
    public void delete(Long id);
    UploadImagemProdutoOutDto gerarUrlPreAssinadaUpload();
    UrlPreassinadaOutDto gerarUrlPreAssinadaVisualizacao(String key);
}
