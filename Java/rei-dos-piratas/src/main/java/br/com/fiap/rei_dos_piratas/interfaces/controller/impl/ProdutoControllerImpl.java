package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.UrlUploadGerada;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio.ProdutoDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UrlPreassinadaOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UploadImagemProdutoOutDto;

import java.util.List;

public class ProdutoControllerImpl implements ProdutoController {

    private final ProdutoService service;

    private final FuncionarioService funcionarioService;

    public ProdutoControllerImpl(ProdutoService service, FuncionarioService funcionarioService) {
        this.service = service;
        this.funcionarioService = funcionarioService;
    }

    @Override
    public ProdutoOutDto findById(Long id) {
        Produto produto = this.service.findById(id);
        return paraDto(produto);
    }

    @Override
    public Page<ProdutoOutDto> findAll(int pageNumber, int pageSize) {

        Page<Produto> produtosPage = this.service.findAll(pageNumber, pageSize);

        List<ProdutoOutDto> produtos = produtosPage
                .pageItems()
                .stream()
                .map(this::paraDto)
                .toList();

        return new Page<ProdutoOutDto>(
                produtosPage.numberOfPages(),
                produtosPage.pageNumber(),
                produtos);
    }


    @Override
    public ProdutoOutDto create(ProdutoInDto produto) {

        Produto produtoEntity = ProdutoDtoMapper.toEntity(produto);

        Funcionario funcionario = this.funcionarioService.findById(produto.funcionarioId());
        produtoEntity.setFuncionario(funcionario);

        Produto novoProduto = this.service.create(produtoEntity);
        return paraDto(novoProduto);
    }

    @Override
    public ProdutoOutDto update(Produto produto) {
        Produto produtoAtualizado = this.service.update(produto);
        return paraDto(produtoAtualizado);
    }

    /** DTO com a URL de exibição da imagem (o produto guarda só a chave). */
    private ProdutoOutDto paraDto(Produto produto) {
        return ProdutoDtoMapper.toDto(produto).comUrlImagem(this.service.urlDaImagem(produto.getEnderecoImagem()));
    }

    @Override
    public void delete(Long id) {
        this.service.delete(id);
    }

    @Override
    public UploadImagemProdutoOutDto gerarUrlPreAssinadaUpload(String contentType) {
        UrlUploadGerada upload = this.service.gerarUrlPreAssinadaUpload(contentType);
        return new UploadImagemProdutoOutDto(upload.key(), upload.url());
    }

    @Override
    public UrlPreassinadaOutDto gerarUrlPreAssinadaVisualizacao(String key) {
        return new UrlPreassinadaOutDto(this.service.gerarUrlPreAssinadaVisualizacao(key));
    }
}
