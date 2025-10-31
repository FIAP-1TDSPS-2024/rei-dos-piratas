package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaCarrinhoEntity;

public class JpaCarrinhoMapper {

    public static Carrinho toEntity(JpaCarrinhoEntity jpaCarrinhoEntity) {
        return new Carrinho(
                jpaCarrinhoEntity.getId(),
                jpaCarrinhoEntity
                        .getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toEntity)
                        .toList()
        );
    }

    public static JpaCarrinhoEntity toJpaEntity(Carrinho carrinho) {
        JpaCarrinhoEntity jpaCarrinho = new JpaCarrinhoEntity(
                carrinho.getId(),
                null,
                carrinho.getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toJpaProdutosCarrinhoEntity)
                        .toList()
        );

        jpaCarrinho
                .getProdutosAdicionados()
                .forEach(produtosCarrinho -> produtosCarrinho.setCarrinho(jpaCarrinho));

        return jpaCarrinho;
    }

    private JpaCarrinhoMapper() {}
}
