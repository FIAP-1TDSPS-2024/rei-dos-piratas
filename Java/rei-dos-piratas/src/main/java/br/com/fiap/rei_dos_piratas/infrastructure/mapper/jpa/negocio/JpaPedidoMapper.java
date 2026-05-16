package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.endereco.JpaEnderecoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.usuarios.JpaClienteMapper;

public class JpaPedidoMapper {

    public static Pedido toEntity(JpaPedidoEntity jpaPedidoEntity) {
        return new Pedido(
                jpaPedidoEntity.getId(),
                jpaPedidoEntity.getDataPedido(),
                jpaPedidoEntity.getDataEntrega(),
                jpaPedidoEntity.getDataPrevisaoEntrega(),
                jpaPedidoEntity.getDataCancelamento(),
                jpaPedidoEntity.getValorTotal(),
                jpaPedidoEntity.getValorFrete(),
                jpaPedidoEntity.getStatus(),
                JpaClienteMapper
                        .toEntity(jpaPedidoEntity.getCliente()),
                jpaPedidoEntity
                        .getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toEntity)
                        .toList(),
                JpaEnderecoMapper.toEntity(jpaPedidoEntity.getEnderecoEntrega()),
                jpaPedidoEntity.getServicoEntrega(),
                jpaPedidoEntity.getNotaFiscal(),
                jpaPedidoEntity.getPedidoFrete(),
                jpaPedidoEntity.getProtocoloEnvio(),
                jpaPedidoEntity.getStatusEnvio(),
                jpaPedidoEntity.getTracking(),
                jpaPedidoEntity.getTrackingUrl());
    }

    public static JpaPedidoEntity toJpaEntity(Pedido pedido) {
        JpaPedidoEntity jpaPedido = new JpaPedidoEntity(
                pedido.getId(),
                pedido.getDataPedido(),
                pedido.getDataEntrega(),
                pedido.getDataPrevisaoEntrega(),
                pedido.getDataCancelamento(),
                pedido.getValorTotal(),
                pedido.getValorFrete(),
                pedido.getStatus(),
                JpaClienteMapper.toJpaEntity(
                        pedido.getCliente(),
                        JpaCarrinhoMapper
                                .toJpaEntity(pedido
                                        .getCliente()
                                        .getCarrinho())),
                pedido.getProdutosAdicionados()
                        .stream()
                        .map(JpaItemProdutoMapper::toJpaProdutosPedidosEntity)
                        .toList(),
                JpaEnderecoMapper.toJpaEntity(pedido.getEnderecoEntrega()),
                pedido.getServicoEntrega(),
                pedido.getNotaFiscal(),
                pedido.getPedidoFrete(),
                pedido.getProtocoloEnvio(),
                pedido.getStatusEnvio(),
                pedido.getTracking(),
                pedido.getTrackingUrl());

        jpaPedido
                .getProdutosAdicionados()
                .forEach(produtosPedido -> produtosPedido.setPedido(jpaPedido));

        return jpaPedido;
    }

    private JpaPedidoMapper() {}
}
