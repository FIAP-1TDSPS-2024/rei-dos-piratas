package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

public interface CarrinhoController {
    CarrinhoOutDto adicionarProduto(ItemProdutoInDto itemProduto);
    CarrinhoOutDto removerProduto(ItemProdutoInDto itemProduto);
    CarrinhoOutDto limparCarrinho();
    CarrinhoOutDto visualizarCarrinho();
    PedidoOutDto finalizarCompra();
}
