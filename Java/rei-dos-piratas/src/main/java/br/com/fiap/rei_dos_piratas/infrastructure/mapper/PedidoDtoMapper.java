package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProduto;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoInDto;

public class PedidoDtoMapper {

    public static Pedido toEntity(Cliente cliente, PedidoInDto pedidoDto) {
        return new Pedido(
                cliente,
                pedidoDto.produtosAdicionados()
                        .stream()
                        .map(ItemProdutoDtoMapper::toEntity)
                        .toList()
        );
    }

    private PedidoDtoMapper() {}
}
