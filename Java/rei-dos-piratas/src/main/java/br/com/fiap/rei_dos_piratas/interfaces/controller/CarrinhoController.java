package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

public interface CarrinhoController {
    CarrinhoOutDto adicionarProduto(Long clienteId, ItemProdutoInDto itemProduto);
    CarrinhoOutDto removerProduto(Long clienteId, ItemProdutoInDto itemProduto);
    CarrinhoOutDto limparCarrinho(Long clienteId);
    CarrinhoOutDto visualizarCarrinho(Long clienteId);
    PedidoOutDto finalizarCompra(Long clienteId);
}
