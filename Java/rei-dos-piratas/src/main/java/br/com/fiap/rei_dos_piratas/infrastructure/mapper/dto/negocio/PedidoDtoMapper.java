package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

import java.util.List;

public class PedidoDtoMapper {

    public static Pedido toEntity(Cliente cliente, Endereco enderecoEntrega, List<ItemProdutoPedido> produtos, PedidoInDto dto) {
        return new Pedido(cliente, enderecoEntrega, produtos, dto.freteService().id(), dto.freteService().price());
    }

    public static PedidoOutDto toDto(Pedido pedido) {
        return new PedidoOutDto(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getDataEntrega(),
                pedido.getDataPrevisaoEntrega(),
                pedido.getDataCancelamento(),
                pedido.getValorTotal(),
                pedido.getValorFrete(),
                pedido.getStatus(),
                pedido.getProdutosAdicionados()
                        .stream()
                        .map(ItemProdutoDtoMapper::toDto)
                        .toList()
        );
    }

    private PedidoDtoMapper() {}
}
