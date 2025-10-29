package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaProdutoEntity;

public class JpaProdutoMapper {

    public static Produto toEntity(JpaProdutoEntity jpaProdutoEntity) {
        return new Produto(
                jpaProdutoEntity.getId(),
                jpaProdutoEntity.getNome(),
                jpaProdutoEntity.getDescricao(),
                jpaProdutoEntity.getEnderecoImagem(),
                jpaProdutoEntity.getPreco(),
                jpaProdutoEntity.getEstoque(),
                jpaProdutoEntity.getAltura(),
                jpaProdutoEntity.getLargura(),
                jpaProdutoEntity.getProfundidade(),
                jpaProdutoEntity.getCondicao(),
                JpaFuncionarioMapper.toEntity(jpaProdutoEntity.getFuncionario())
        );
    }

    public static JpaProdutoEntity toJpaEntity(Produto produto) {
        return new JpaProdutoEntity(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getEnderecoImagem(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getAltura(),
                produto.getLargura(),
                produto.getProfundidade(),
                produto.getCondicao(),
                JpaFuncionarioMapper.toJpaEntity(produto.getFuncionario())
        );
    }

    private JpaProdutoMapper() {}
}
