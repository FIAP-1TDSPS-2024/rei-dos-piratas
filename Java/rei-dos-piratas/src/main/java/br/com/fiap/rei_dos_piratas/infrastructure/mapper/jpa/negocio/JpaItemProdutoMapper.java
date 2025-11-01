package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoCarrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutosCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutosPedidoEntity;

public class JpaItemProdutoMapper {

    public static ItemProdutoPedido toPedido(ItemProdutoCarrinho itemProdutoCarrinho) {
        return new ItemProdutoPedido(
                itemProdutoCarrinho.getProduto(),
                itemProdutoCarrinho.getQuantidade());
    }

    public static ItemProdutoCarrinho toEntity(JpaProdutosCarrinhoEntity jpaProdutosCarrinhoEntity) {
        return new ItemProdutoCarrinho(
                jpaProdutosCarrinhoEntity.getId(),
                JpaProdutoMapper.toEntity(jpaProdutosCarrinhoEntity.getProduto()),
                jpaProdutosCarrinhoEntity.getQuantidade());
    }

    public static ItemProdutoPedido toEntity(JpaProdutosPedidoEntity jpaProdutosPedidoEntity) {
        return new ItemProdutoPedido(
                jpaProdutosPedidoEntity.getId(),
                JpaProdutoMapper.toEntity(jpaProdutosPedidoEntity.getProduto()),
                jpaProdutosPedidoEntity.getQuantidade());
    }

    public static JpaProdutosPedidoEntity toJpaProdutosPedidosEntity(ItemProdutoPedido itemProdutoPedido){
        return new JpaProdutosPedidoEntity(
                itemProdutoPedido.getId(),
                JpaProdutoMapper.toJpaEntity(itemProdutoPedido.getProduto()),
                null,
                itemProdutoPedido.getQuantidade()
        );
    }

    public static JpaProdutosCarrinhoEntity toJpaProdutosCarrinhoEntity(ItemProdutoCarrinho itemProdutoPedido){
        return new JpaProdutosCarrinhoEntity(
                itemProdutoPedido.getId(),
                JpaProdutoMapper.toJpaEntity(itemProdutoPedido.getProduto()),
                null,
                itemProdutoPedido.getQuantidade()
        );
    }

    private JpaItemProdutoMapper() {}
}
