package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.application.service.EventoExternoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Caracterização do roteamento dos eventos de cobrança (idempotência via EventoExterno). */
class RastreioCobrancaServiceImplTest {

    private PedidoService pedidoService;
    private EstornoService estornoService;
    private EventoExternoService eventoExternoService;
    private RastreioCobrancaService service;

    @BeforeEach
    void setUp() {
        pedidoService = mock(PedidoService.class);
        estornoService = mock(EstornoService.class);
        eventoExternoService = mock(EventoExternoService.class);
        service = new RastreioCobrancaServiceImpl(pedidoService, estornoService, eventoExternoService);

        EventoExterno registro = new EventoExterno(77L, OrigemEventoEnum.ASAAS, "evt_1", "PAYMENT_CONFIRMED",
                LocalDateTime.now(), null, "{}", 1, null);
        when(eventoExternoService.iniciarProcessamento(any(), any(), any(), any())).thenReturn(Optional.of(registro));
    }

    private EventoPagamento evento(String id, TipoEventoPagamento tipo, String tipoOriginal) {
        return new EventoPagamento(id, tipo, tipoOriginal, LocalDateTime.of(2026, 11, 4, 10, 0), "pay_1", 10L, null);
    }

    @Test
    void eventoDeCobrancaDeveIrParaOPedidoEConcluirORegistro() {
        // Act
        service.processarEvento(evento("evt_1", TipoEventoPagamento.CONFIRMADA, "PAYMENT_CONFIRMED"), "{}");

        // Assert
        verify(eventoExternoService).iniciarProcessamento(OrigemEventoEnum.ASAAS, "evt_1", "PAYMENT_CONFIRMED", "{}");
        verify(pedidoService).processarEventoCobranca(eq(10L), any());
        verify(eventoExternoService).concluir(77L);
        verifyNoInteractions(estornoService);
    }

    @Test
    void eventoDeEstornoDeveIrParaOServicoDeEstorno() {
        // Act
        service.processarEvento(evento("evt_2", TipoEventoPagamento.ESTORNADA, "PAYMENT_REFUNDED"), "{}");

        // Assert
        verify(estornoService).processarEventoEstorno(eq(10L), any());
        verify(pedidoService, never()).processarEventoCobranca(any(), any());
        verify(eventoExternoService).concluir(77L);
    }

    @Test
    void eventoDuplicadoNaoDeveSerProcessadoDeNovo() {
        // Arrange
        when(eventoExternoService.iniciarProcessamento(any(), any(), any(), any())).thenReturn(Optional.empty());

        // Act
        service.processarEvento(evento("evt_1", TipoEventoPagamento.CONFIRMADA, "PAYMENT_CONFIRMED"), "{}");

        // Assert
        verifyNoInteractions(pedidoService, estornoService);
    }

    @Test
    void falhaNoProcessamentoDeveMarcarOEventoComoFalhoEPropagar() {
        // Arrange
        doThrow(new RuntimeException("banco fora")).when(pedidoService).processarEventoCobranca(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> service.processarEvento(evento("evt_1", TipoEventoPagamento.CONFIRMADA, "PAYMENT_CONFIRMED"), "{}"))
                .isInstanceOf(RuntimeException.class);
        verify(eventoExternoService).falhar(77L);
        verify(eventoExternoService, never()).concluir(any());
    }

    @Test
    void eventoNuloDeveSerIgnorado() {
        service.processarEvento(null, "{}");

        verifyNoInteractions(eventoExternoService, pedidoService, estornoService);
    }



}
