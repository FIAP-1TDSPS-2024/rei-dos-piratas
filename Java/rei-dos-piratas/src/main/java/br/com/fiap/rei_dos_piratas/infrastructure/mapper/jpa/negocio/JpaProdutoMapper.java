package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaFuncionarioMapper;

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
                jpaProdutoEntity.getPeso(),
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
                produto.getPeso(),
                produto.getCondicao(),
                JpaFuncionarioMapper.toJpaEntity(produto.getFuncionario())
        );
    }

    private JpaProdutoMapper() {}
}
