package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;

public interface CarrinhoService {
    Carrinho adicionarProduto(Long clienteId, ItemProdutoPedido itemProdutoPedido);
    Carrinho removerProduto(Long clienteId, ItemProdutoPedido itemProdutoPedido);
    Carrinho limparCarrinho(Long clienteId);
    Carrinho visualizarCarrinho(Long clienteId);
    Pedido finalizarCompra(Long clienteId);
}
