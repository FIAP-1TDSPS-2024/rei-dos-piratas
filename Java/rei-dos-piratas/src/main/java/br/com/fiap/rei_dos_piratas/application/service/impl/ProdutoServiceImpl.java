package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;
    private final Validator validator;

    public ProdutoServiceImpl(ProdutoRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public Produto findById(Long id) {
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
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
        validar(produto);
        return this.repository.create(produto);
    }

    @Transactional
    @Override
    public Produto update(Produto produto) {
        validar(produto);
        Produto produtoAtualizado = this.repository.update(produto);
        if (produtoAtualizado == null) {
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + produto.getId() + ". Crie um novo produto.");
        }
        return produtoAtualizado;
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }

    /**
     * Valida o objeto Produto contra as anotações da entidade de domínio.
     * Lança ValidacaoException com o mapa campo → mensagem caso haja violações.
     * Centraliza a validação para qualquer entrada (API REST ou formulário web).
     */
    private void validar(Produto produto) {
        Set<ConstraintViolation<Produto>> violacoes = validator.validate(produto);
        if (!violacoes.isEmpty()) {
            Map<String, String> erros = violacoes.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage,
                            (m1, m2) -> m1   // em caso de duplicata, mantém a primeira
                    ));
            throw new ValidacaoException(erros);
        }
    }
}
