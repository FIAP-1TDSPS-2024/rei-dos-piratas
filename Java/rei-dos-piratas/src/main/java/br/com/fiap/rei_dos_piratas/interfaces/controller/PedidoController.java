package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;

public interface PedidoController {
    public Page<PedidoOutDto> findAllByCliente(int pageNumber, int pageSize, Long clienteId);
    public PedidoOutDto findById(Long id);
    public PedidoOutDto fazerPedido(PedidoInDto pedido, Long clienteId);
    public PedidoOutDto pagarPedido(Long id);
    public PedidoOutDto enviarPedido(Long id);
    public PedidoOutDto entregarPedido(Long id);
    public PedidoOutDto cancelarPedido(Long id);
}
