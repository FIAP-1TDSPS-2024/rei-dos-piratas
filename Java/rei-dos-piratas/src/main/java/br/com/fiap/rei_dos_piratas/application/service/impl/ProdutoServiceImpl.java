package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ValidacaoException;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;
    private final Validator validator;

    public ProdutoServiceImpl(ProdutoRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Override
    public Produto findById(Long id) {
        log.debug("[PRODUTO] Buscando produto por ID={}", id);
        try {
            return this.repository.findById(id);
        } catch (NoSuchElementException e) {
            log.warn("[PRODUTO] Produto não encontrado: ID={}", id);
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + id);
        }
    }

    @Override
    public Page<Produto> findAll(int pageNumber, int pageSize) {
        log.debug("[PRODUTO] Listando todos os produtos - página: {}, tamanho: {}", pageNumber, pageSize);
        return this.repository.listAll(pageNumber, pageSize);
    }


    @Transactional
    @Override
    public Produto create(Produto produto) {
        log.info("[PRODUTO] Criando novo produto - nome='{}', preço={}, estoque={}",
                produto.getNome(), produto.getPreco(), produto.getEstoque());
        validar(produto);
        Produto produtoCriado = this.repository.create(produto);
        log.info("[PRODUTO] Produto criado com sucesso - ID={}, nome='{}'", produtoCriado.getId(), produtoCriado.getNome());
        return produtoCriado;
    }

    @Transactional
    @Override
    public Produto update(Produto produto) {
        log.info("[PRODUTO] Atualizando produto ID={}", produto.getId());
        validar(produto);
        Produto produtoAtualizado = this.repository.update(produto);
        if (produtoAtualizado == null) {
            log.error("[PRODUTO] Falha ao atualizar produto ID={} - registro não encontrado", produto.getId());
            throw new ResourceNotFoundException("Não foi possível encontrar um produto com o id " + produto.getId() + ". Crie um novo produto.");
        }
        log.info("[PRODUTO] Produto ID={} atualizado com sucesso - nome='{}'", produtoAtualizado.getId(), produtoAtualizado.getNome());
        return produtoAtualizado;
    }

    @Override
    public void delete(Long id) {
        log.info("[PRODUTO] Removendo produto ID={}", id);
        this.repository.delete(id);
        log.info("[PRODUTO] Produto ID={} removido com sucesso", id);
    }

    /**
     * Valida o objeto Produto contra as anotações da entidade de domínio.
     * Lança ValidacaoException com o mapa campo → mensagem caso haja violações.
     * Centraliza a validação para qualquer entrada (API REST ou formulário web).
     */
    private void validar(Produto produto) {
        Set<ConstraintViolation<Produto>> violacoes = validator.validate(produto);
        if (!violacoes.isEmpty()) {
            log.warn("[PRODUTO] Validação falhou para produto nome='{}' - {} violação(ões)", produto.getNome(), violacoes.size());
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
