package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

public interface CarrinhoController {
    CarrinhoOutDto adicionarProduto(ItemProdutoInDto itemProduto);
    CarrinhoOutDto removerProduto(ItemProdutoInDto itemProduto);
    CarrinhoOutDto limparCarrinho();
    CarrinhoOutDto visualizarCarrinho();
    PedidoOutDto finalizarCompra();
}
