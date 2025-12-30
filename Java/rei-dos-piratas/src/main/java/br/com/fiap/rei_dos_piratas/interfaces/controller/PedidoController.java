package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

public interface PedidoController {
    public Page<PedidoOutDto> findAllByCliente(int pageNumber, int pageSize);
    public PedidoOutDto findById(Long id);
    public PedidoOutDto fazerPedido(PedidoInDto pedido);
    public PedidoOutDto pagarPedido(Long id);
    public PedidoOutDto enviarPedido(Long id);
    public PedidoOutDto entregarPedido(Long id);
    public PedidoOutDto cancelarPedido(Long id);
}
