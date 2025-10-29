package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProduto;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaPedidoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaProdutoEntity;

public class JpaPedidoMapper {

    public static Pedido toEntity(JpaPedidoEntity jpaPedidoEntity) {
        return new Pedido(
                jpaPedidoEntity.getId(),
                jpaPedidoEntity.getDataPedido(),
                jpaPedidoEntity.getDataEntrega(),
                jpaPedidoEntity.getDataCancelamento(),
                jpaPedidoEntity.getValorTotal(),
                jpaPedidoEntity.getStatus(),
                JpaClienteMapper
                        .toEntity(jpaPedidoEntity.getCliente()),
                jpaPedidoEntity
                        .getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toEntity)
                        .toList()
        );
    }

    public static JpaPedidoEntity toJpaEntity(Pedido pedido) {
        JpaPedidoEntity jpaPedido = new JpaPedidoEntity(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getDataEntrega(),
                pedido.getDataCancelamento(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                JpaClienteMapper.toJpaEntity(
                        pedido.getCliente(),
                        JpaEnderecoMapper
                                .toJpaEntity(pedido
                                        .getCliente()
                                        .getEndereco())),
                pedido.getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toJpaProdutosPedidosEntity)
                        .toList()
        );

        jpaPedido
                .getProdutosAdicionados()
                .forEach(produtosPedido -> produtosPedido.setPedido(jpaPedido));

        return jpaPedido;
    }

    private JpaPedidoMapper() {}
}
