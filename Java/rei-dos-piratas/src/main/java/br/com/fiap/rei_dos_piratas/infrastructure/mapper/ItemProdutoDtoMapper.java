package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProduto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoOutDto;

public class ItemProdutoDtoMapper {

    public static ItemProdutoOutDto toDto(ItemProduto itemProduto) {
        return new ItemProdutoOutDto(
                ProdutoDtoMapper.toDto(itemProduto.getProduto()),
                itemProduto.getQuantidade()
        );
    }

    private ItemProdutoDtoMapper() {}
}
