package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

public interface CarrinhoController {
    CarrinhoOutDto adicionarProduto(Long clienteId, Long produtoId, int quantidade);
    CarrinhoOutDto removerProduto(Long clienteId, Long produtoId, int quantidade);
    CarrinhoOutDto limparCarrinho(Long clienteId);
    CarrinhoOutDto visualizarCarrinho(Long clienteId);
    PedidoOutDto finalizarCompra(Long clienteId);
}
