package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;

public interface CarrinhoService {
    Carrinho adicionarProduto(ItemProdutoPedido itemProdutoPedido);
    Carrinho removerProduto(ItemProdutoPedido itemProdutoPedido);
    Carrinho limparCarrinho();
    Carrinho visualizarCarrinho();
    Pedido finalizarCompra(Endereco enderecoEntrega, FreteServiceDto freteServiceDto);
}
