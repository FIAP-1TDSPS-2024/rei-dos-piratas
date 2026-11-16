package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaFuncionarioMapper;

public class JpaProdutoMapper {

    public static Produto toEntity(JpaProdutoEntity jpaProdutoEntity) {
        Produto produto = new Produto(
                jpaProdutoEntity.getId(),
                jpaProdutoEntity.getNome(),
                jpaProdutoEntity.getDescricao(),
                jpaProdutoEntity.getAutor(),
                jpaProdutoEntity.getCategoria(),
                jpaProdutoEntity.getEnderecoImagem(),
                jpaProdutoEntity.getPreco(),
                jpaProdutoEntity.getPrecoOriginal(),
                jpaProdutoEntity.getEstoque(),
                jpaProdutoEntity.getAltura(),
                jpaProdutoEntity.getLargura(),
                jpaProdutoEntity.getProfundidade(),
                jpaProdutoEntity.getPeso(),
                jpaProdutoEntity.getCondicao(),
                JpaFuncionarioMapper.toEntity(jpaProdutoEntity.getFuncionario())
        );
        produto.setAtivo(jpaProdutoEntity.isAtivo());
        return produto;
    }

    public static JpaProdutoEntity toJpaEntity(Produto produto) {
        return new JpaProdutoEntity(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getAutor(),
                produto.getCategoria(),
                produto.getEnderecoImagem(),
                produto.getPreco(),
                produto.getPrecoOriginal(),
                produto.getEstoque(),
                produto.getAltura(),
                produto.getLargura(),
                produto.getProfundidade(),
                produto.getPeso(),
                produto.getCondicao(),
                JpaFuncionarioMapper.toJpaEntity(produto.getFuncionario()),
                produto.isAtivo()
        );
    }

    private JpaProdutoMapper() {}
}
