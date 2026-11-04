package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.messaging;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.TradutorEventoAsaas;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventoCobrancaListenerTest {

    private RastreioCobrancaService rastreioCobrancaService;
    private EventoCobrancaListener listener;

    @BeforeEach
    void setUp() {
        rastreioCobrancaService = mock(RastreioCobrancaService.class);
        // Mesmo naming global do projeto (snake_case); o DTO do Asaas declara os campos camelCase
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        listener = new EventoCobrancaListener(objectMapper, new TradutorEventoAsaas(), rastreioCobrancaService);
    }

    @Test
    void mensagemDoAsaasDeveChegarAoServicoComoEventoDeDominio() {
        // Arrange
        String mensagem = """
                {"id":"evt_05b708f961d739ea7eba7e4db318f621","event":"PAYMENT_CREDIT_CARD_CAPTURE_REFUSED",
                 "dateCreated":"2026-11-04 10:30:00",
                 "payment":{"id":"pay_080225913252","status":"PENDING","externalReference":"42","customer":"cus_1"},
                 "additionalInfo":{"denialReason":"Saldo insuficiente"}}
                """;

        // Act
        listener.onMessage(mensagem);

        // Assert
        ArgumentCaptor<EventoPagamento> captor = ArgumentCaptor.forClass(EventoPagamento.class);
        verify(rastreioCobrancaService).processarEvento(captor.capture(), eq(mensagem));
        EventoPagamento evento = captor.getValue();
        assertThat(evento.idEvento()).isEqualTo("evt_05b708f961d739ea7eba7e4db318f621");
        assertThat(evento.tipo()).isEqualTo(TipoEventoPagamento.RECUSADA);
        assertThat(evento.ocorridoEm()).isEqualTo(LocalDateTime.of(2026, 11, 4, 10, 30));
        assertThat(evento.idCobranca()).isEqualTo("pay_080225913252");
        assertThat(evento.pedidoId()).isEqualTo(42L);
        assertThat(evento.motivoRecusa()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void eventoIgnoradoPeloTradutorNaoDeveChegarAoServico() {
        listener.onMessage("""
                {"id":"evt_1","event":"PAYMENT_CONFIRMED","payment":{"id":"pay_1"}}
                """);

        verifyNoInteractions(rastreioCobrancaService);
    }

    @Test
    void mensagemInvalidaNaoDeveLancarExcecaoNemChegarAoServico() {
        assertThatCode(() -> listener.onMessage("isto não é json")).doesNotThrowAnyException();

        verifyNoInteractions(rastreioCobrancaService);
    }
}
