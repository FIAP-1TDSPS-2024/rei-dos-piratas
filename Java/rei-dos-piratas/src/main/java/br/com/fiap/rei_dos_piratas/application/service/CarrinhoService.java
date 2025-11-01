package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;

public interface CarrinhoService {
    Carrinho adicionarProduto(Long clienteId, Long produtoId, int quantidade);
    Carrinho removerProduto(Long clienteId, Long produtoId, int quantidade);
    Carrinho limparCarrinho(Long clienteId);
    Carrinho visualizarCarrinho(Long clienteId);
    Pedido finalizarCompra(Long clienteId);
}
