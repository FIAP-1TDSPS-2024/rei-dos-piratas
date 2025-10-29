package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProduto;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaProdutosCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaProdutosPedidoEntity;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

public class JpaItemProdutoMapper {

    public static ItemProduto toEntity(JpaProdutosCarrinhoEntity jpaProdutosCarrinhoEntity) {
        return new ItemProduto(
                jpaProdutosCarrinhoEntity.getId(),
                JpaProdutoMapper.toEntity(jpaProdutosCarrinhoEntity.getProduto()),
                jpaProdutosCarrinhoEntity.getQuantidade());
    }

    public static ItemProduto toEntity(JpaProdutosPedidoEntity jpaProdutosPedidoEntity) {
        return new ItemProduto(
                jpaProdutosPedidoEntity.getId(),
                JpaProdutoMapper.toEntity(jpaProdutosPedidoEntity.getProduto()),
                jpaProdutosPedidoEntity.getQuantidade());
    }

    public static JpaProdutosPedidoEntity toJpaProdutosPedidosEntity(ItemProduto itemProduto){
        return new JpaProdutosPedidoEntity(
                itemProduto.getId(),
                JpaProdutoMapper.toJpaEntity(itemProduto.getProduto()),
                null,
                itemProduto.getQuantidade()
        );
    }

    public static JpaProdutosCarrinhoEntity toJpaProdutosCarrinhoEntity(ItemProduto itemProduto){
        return new JpaProdutosCarrinhoEntity(
                itemProduto.getId(),
                JpaProdutoMapper.toJpaEntity(itemProduto.getProduto()),
                null,
                itemProduto.getQuantidade()
        );
    }

    private JpaItemProdutoMapper() {}
}
