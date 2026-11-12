package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

public interface ProdutoService {
    Produto findById(Long id);
    Page<Produto> findAll(int pageNumber, int pageSize);
    Produto create(Produto produto);
    Produto update(Produto produto);
    void delete(Long id);
    UrlUploadGerada gerarUrlPreAssinadaUpload();
    String gerarUrlPreAssinadaVisualizacao(String key);
}
