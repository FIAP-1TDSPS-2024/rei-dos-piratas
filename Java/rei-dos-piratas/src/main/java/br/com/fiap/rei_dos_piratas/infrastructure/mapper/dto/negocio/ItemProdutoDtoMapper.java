package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoCarrinho;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoOutDto;

public class ItemProdutoDtoMapper {

    public static ItemProdutoOutDto toDto(ItemProdutoPedido itemProdutoPedido) {
        return new ItemProdutoOutDto(
                ProdutoDtoMapper.toDto(itemProdutoPedido.getProduto()),
                itemProdutoPedido.getQuantidade()
        );
    }

    public static ItemProdutoOutDto toDto(ItemProdutoCarrinho itemProdutoCarrinho) {
        return new ItemProdutoOutDto(
                ProdutoDtoMapper.toDto(itemProdutoCarrinho.getProduto()),
                itemProdutoCarrinho.getQuantidade()
        );
    }

    private ItemProdutoDtoMapper() {}
}
