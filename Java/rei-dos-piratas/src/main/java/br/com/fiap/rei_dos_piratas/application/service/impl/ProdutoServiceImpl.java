package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Produto findById(Long id) {
        try {
            return this.repository.findById(id);
        }
        catch (NoSuchElementException e){
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + id);
        }
    }

    @Override
    public Page<Produto> findAll(int pageNumber, int pageSize) {
        return this.repository.listAll(pageNumber, pageSize);
    }

    @Transactional
    @Override
    public Produto create(Produto produto) {
        return this.repository.create(produto);
    }

    @Transactional
    @Override
    public Produto update(Produto produto) {
        Produto produtoAtualizado = this.repository.update(produto);

        if (produtoAtualizado == null){
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + produto.getId() + ". Crie um novo produto.");
        }

        return produtoAtualizado;
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }
}
