package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;

public class CarrinhoDtoMapper {

    public static CarrinhoOutDto toDto(Carrinho carrinho) {
        if (carrinho == null){
            return null;
        }
        return new CarrinhoOutDto(
                carrinho.getId(),
                carrinho
                        .getProdutosAdicionados()
                        .stream()
                        .map(ItemProdutoDtoMapper::toDto)
                        .toList());
    }

    private CarrinhoDtoMapper() {}
}
