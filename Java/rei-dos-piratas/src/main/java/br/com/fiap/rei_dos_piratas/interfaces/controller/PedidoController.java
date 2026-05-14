package br.com.fiap.rei_dos_piratas.interfaces.controller;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;

import java.util.List;
import java.util.Map;

public interface PedidoController {
    Page<PedidoOutDto> findAllByCliente(int pageNumber, int pageSize);
    Page<PedidoOutDto> findAllByStatus(int pageNumber, int pageSize, StatusEnum status);
    PedidoOutDto findById(Long id);
    PedidoOutDto fazerPedido(PedidoInDto pedido);
    PedidoOutDto pagarPedido(Long id);
    String organizarPedidosParaEnvio(List<Long> pedidos);
    Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos);
    String imprimirEtiquetasEnvio(List<Long> pedidos);
    void rastreioPedidoWebhook(String signature, String rawBody);
    PedidoOutDto cancelarPedido(Long id);
}
