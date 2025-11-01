package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

import java.util.List;

public class PedidoDtoMapper {

    public static Pedido toEntity(Cliente cliente, List<ItemProdutoPedido> produtos) {
        return new Pedido(cliente, produtos);
    }

    public static PedidoOutDto toDto(Pedido pedido) {
        return new PedidoOutDto(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getDataEntrega(),
                pedido.getDataCancelamento(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getProdutosAdicionados()
                        .stream()
                        .map(ItemProdutoDtoMapper::toDto)
                        .toList()
        );
    }

    private PedidoDtoMapper() {}
}
