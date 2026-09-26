package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PedidoService {
    Page<Pedido> findAll(int pageNumber, int pageSize);
    Page<Pedido> findAllByStatus(int pageNumber, int pageSize, StatusEnum status);
    Pedido findById(Long id);
    Pedido fazerPedido(Pedido pedido);
    Pedido pagarPedido(Long id);
    String organizarPedidosParaEnvio(List<Long> pedidos);
    Map<Long, String> gerarEtiquetasParaEnvio(List<Long> pedidos);
    String imprimirEtiquetasEnvio(List<Long> pedidos);
    void rastreioPedidoWebhook(Pedido pedido, RastreioWebhookDto rastreio);
    Optional<Pedido> findByPedidoFrete(UUID pedidoFrete);
    Pedido cancelarPedido(Long id);
}
