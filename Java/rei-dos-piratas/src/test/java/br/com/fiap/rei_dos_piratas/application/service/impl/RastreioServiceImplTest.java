package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Roteamento do evento de rastreio para o pedido ou a devolução dona do envio.
 * Assinatura e leitura do corpo ficaram no adaptador (WebhookRastreioMelhorEnvioTest, TradutorEventoRastreioTest).
 */
class RastreioServiceImplTest {

    private static final UUID ENVIO = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");
    private static final EventoRastreio POSTADO = new EventoRastreio(TipoEventoRastreio.POSTADO, "order.posted", ENVIO,
            "posted", "ORD-1", "BR123", null, null);

    private PedidoService pedidoService;
    private DevolucaoService devolucaoService;
    private RastreioServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoService = mock(PedidoService.class);
        devolucaoService = mock(DevolucaoService.class);
        service = new RastreioServiceImpl(pedidoService, devolucaoService);
        when(pedidoService.findByPedidoFrete(any())).thenReturn(Optional.empty());
        when(devolucaoService.findByPedidoFrete(any())).thenReturn(Optional.empty());
    }

    @Test
    void envioDePedidoDeveIrParaOPedido() {
        // Arrange
        Pedido pedido = new Pedido();
        when(pedidoService.findByPedidoFrete(ENVIO)).thenReturn(Optional.of(pedido));

        // Act
        service.processarEvento(POSTADO);

        // Assert
        verify(pedidoService).rastreioPedidoWebhook(pedido, POSTADO);
        verify(devolucaoService, never()).rastreioDevolucaoWebhook(any(), any());
    }

    @Test
    void envioDeDevolucaoDeveIrParaADevolucao() {
        // Arrange
        Devolucao devolucao = new Devolucao();
        when(devolucaoService.findByPedidoFrete(ENVIO)).thenReturn(Optional.of(devolucao));

        // Act
        service.processarEvento(POSTADO);

        // Assert
        verify(devolucaoService).rastreioDevolucaoWebhook(devolucao, POSTADO);
        verify(pedidoService, never()).rastreioPedidoWebhook(any(), any());
    }

    @Test
    void envioDesconhecidoNaoDeveAtualizarNada() {
        service.processarEvento(POSTADO);

        verify(pedidoService, never()).rastreioPedidoWebhook(any(), any());
        verify(devolucaoService, never()).rastreioDevolucaoWebhook(any(), any());
    }
}
