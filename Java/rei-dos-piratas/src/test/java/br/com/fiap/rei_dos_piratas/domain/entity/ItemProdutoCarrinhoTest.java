package br.com.fiap.rei_dos_piratas.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ItemProdutoCarrinhoTest {

    private Produto produtoComPreco(String preco) {
        Produto produto = new Produto(7L);
        produto.setPreco(new BigDecimal(preco));
        return produto;
    }

    @Test
    @DisplayName("Converte item do carrinho em item de pedido com o mesmo produto e quantidade")
    void deveConverterEmItemDePedidoComMesmoProdutoEQuantidade() {
        // Arrange
        Produto produto = produtoComPreco("25.50");
        ItemProdutoCarrinho itemCarrinho = new ItemProdutoCarrinho(1L, produto, 3);

        // Act
        ItemProdutoPedido itemPedido = itemCarrinho.paraItemPedido();

        // Assert
        assertThat(itemPedido.getProduto()).isSameAs(produto);
        assertThat(itemPedido.getQuantidade()).isEqualTo(3);
    }

    @Test
    @DisplayName("Item de pedido ainda não persistido não herda o id do item do carrinho")
    void deveCriarItemDePedidoSemId() {
        ItemProdutoCarrinho itemCarrinho = new ItemProdutoCarrinho(1L, produtoComPreco("25.50"), 1);

        ItemProdutoPedido itemPedido = itemCarrinho.paraItemPedido();

        assertThat(itemPedido.getId()).isNull();
    }

    @Test
    @DisplayName("Comportamento atual: o pedido usa o preço vigente do produto, não o preço gravado no carrinho")
    void deveUsarPrecoAtualDoProdutoAoConverterParaPedido() {
        Produto produto = produtoComPreco("25.50");
        ItemProdutoCarrinho itemCarrinho = new ItemProdutoCarrinho(1L, produto, 2, new BigDecimal("20.00"));

        ItemProdutoPedido itemPedido = itemCarrinho.paraItemPedido();

        assertThat(itemPedido.getPrecoUnitario()).isEqualByComparingTo("25.50");
    }
}
