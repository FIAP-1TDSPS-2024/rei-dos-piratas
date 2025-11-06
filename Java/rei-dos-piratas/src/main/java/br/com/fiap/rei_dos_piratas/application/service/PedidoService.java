package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;

public interface PedidoService {
    public Page<Pedido> findAll(int pageNumber, int pageSize);
    public Pedido findById(Long id);
    public Pedido fazerPedido(Pedido pedido);
    public Pedido pagarPedido(Long id);
    public Pedido enviarPedido(Long id);
    public Pedido entregarPedido(Long id);
    public Pedido cancelarPedido(Long id);
}
