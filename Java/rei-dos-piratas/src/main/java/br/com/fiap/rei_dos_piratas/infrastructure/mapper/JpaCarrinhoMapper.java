package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaCarrinhoEntity;

public class JpaCarrinhoMapper {

    public static Carrinho toEntity(JpaCarrinhoEntity jpaCarrinhoEntity) {
        return new Carrinho(
                jpaCarrinhoEntity.getId(),
                JpaClienteMapper
                        .toEntity(jpaCarrinhoEntity.getCliente()),
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
                JpaClienteMapper.toJpaEntity(
                                carrinho.getCliente(),
                                JpaEnderecoMapper
                                        .toJpaEntity(carrinho
                                                        .getCliente()
                                                        .getEndereco())),
                carrinho.getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toJpaProdutosCarrinhoEntity)
                        .toList()
        );

        jpaCarrinho
                .getProdutosAdicionados()
                .forEach(produtosPedido -> produtosPedido.setCarrinho(jpaCarrinho));

        return jpaCarrinho;
    }

    private JpaCarrinhoMapper() {}
}
