package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProduto;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;

public class ItemProdutoDtoMapper {

    public static ItemProduto toEntity(ItemProdutoInDto dto){
        return new ItemProduto(
                dto.produto(),
                dto.quantidade()
        );
    }

    private ItemProdutoDtoMapper() {}
}
