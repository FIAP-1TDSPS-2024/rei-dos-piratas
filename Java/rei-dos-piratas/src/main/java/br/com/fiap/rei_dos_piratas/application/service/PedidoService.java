package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;

public interface PedidoService {
    public Page<Pedido> findAllByCliente(int pageNumber, int pageSize, Long clienteId);
    public Pedido findById(Long id);
    public Pedido fazerPedido(Pedido pedido);
    public Pedido pagarPedido(Pedido pedido);
    public Pedido enviarPedido(Pedido pedido);
    public Pedido entregarPedido(Pedido pedido);
    public Pedido cancelarPedido(Pedido pedido);
}
