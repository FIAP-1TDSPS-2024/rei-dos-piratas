package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.EventoCobrancaDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TradutorEventoAsaasTest {

    private final TradutorEventoAsaas tradutor = new TradutorEventoAsaas();
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 11, 4, 10, 0);

    private EventoCobrancaDto evento(String id, String tipo, String referencia, String motivo) {
        return new EventoCobrancaDto(id, tipo, DATA,
                new EventoCobrancaDto.Payment("pay_1", "CONFIRMED", referencia),
                motivo == null ? null : new EventoCobrancaDto.AdditionalInfo(motivo));
    }

    @Test
    void deveTraduzirOsDadosDoEvento() {
        // Act
        EventoPagamento traduzido = tradutor.traduzir(evento("evt_1", "PAYMENT_CREDIT_CARD_CAPTURE_REFUSED", "10", "Saldo insuficiente")).orElseThrow();

        // Assert
        assertThat(traduzido.idEvento()).isEqualTo("evt_1");
        assertThat(traduzido.tipo()).isEqualTo(TipoEventoPagamento.RECUSADA);
        assertThat(traduzido.tipoOriginal()).isEqualTo("PAYMENT_CREDIT_CARD_CAPTURE_REFUSED");
        assertThat(traduzido.ocorridoEm()).isEqualTo(DATA);
        assertThat(traduzido.idCobranca()).isEqualTo("pay_1");
        assertThat(traduzido.pedidoId()).isEqualTo(10L);
        assertThat(traduzido.motivoRecusa()).isEqualTo("Saldo insuficiente");
    }

    @ParameterizedTest
    @CsvSource({
            "PAYMENT_CREATED, CRIADA",
            "PAYMENT_AUTHORIZED, AUTORIZADA",
            "PAYMENT_CONFIRMED, CONFIRMADA",
            "PAYMENT_RECEIVED, RECEBIDA",
            "PAYMENT_CREDIT_CARD_CAPTURE_REFUSED, RECUSADA",
            "PAYMENT_REPROVED_BY_RISK_ANALYSIS, RECUSADA",
            "PAYMENT_OVERDUE, VENCIDA",
            "PAYMENT_DELETED, REMOVIDA",
            "PAYMENT_AWAITING_RISK_ANALYSIS, OUTRO",
            "PAYMENT_APPROVED_BY_RISK_ANALYSIS, OUTRO",
            "PAYMENT_UPDATED, OUTRO",
            "PAYMENT_ANTICIPATED, OUTRO",
            "PAYMENT_RESTORED, OUTRO",
            "PAYMENT_REFUNDED, ESTORNADA",
            "PAYMENT_PARTIALLY_REFUNDED, ESTORNO_PARCIAL",
            "PAYMENT_REFUND_IN_PROGRESS, ESTORNO_EM_ANDAMENTO",
            "PAYMENT_REFUND_DENIED, ESTORNO_NEGADO",
            "PAYMENT_RECEIVED_IN_CASH_UNDONE, OUTRO_ESTORNO",
            "PAYMENT_CHARGEBACK_REQUESTED, OUTRO_ESTORNO",
            "PAYMENT_CHARGEBACK_DISPUTE, OUTRO_ESTORNO",
            "PAYMENT_AWAITING_CHARGEBACK_REVERSAL, OUTRO_ESTORNO"
    })
    void deveMapearCadaEventoDoAsaas(String eventoAsaas, TipoEventoPagamento esperado) {
        assertThat(tradutor.traduzir(evento("evt_1", eventoAsaas, "10", null)))
                .map(EventoPagamento::tipo)
                .contains(esperado);
    }

    @Test
    void eventoNuloDeveSerIgnorado() {
        assertThat(tradutor.traduzir(null)).isEmpty();
    }

    @Test
    void eventoSemIdDeveSerIgnorado() {
        assertThat(tradutor.traduzir(evento(" ", "PAYMENT_CONFIRMED", "10", null))).isEmpty();
    }

    @Test
    void eventoSemTipoDeveSerIgnorado() {
        assertThat(tradutor.traduzir(evento("evt_1", null, "10", null))).isEmpty();
    }

    @Test
    void eventoSemCobrancaDeveSerIgnorado() {
        assertThat(tradutor.traduzir(new EventoCobrancaDto("evt_1", "PAYMENT_CONFIRMED", DATA, null, null))).isEmpty();
    }

    @Test
    void eventoSemReferenciaDoPedidoDeveSerIgnorado() {
        assertThat(tradutor.traduzir(evento("evt_1", "PAYMENT_CONFIRMED", null, null))).isEmpty();
    }

    @Test
    void tipoDeEventoDesconhecidoDeveSerIgnorado() {
        assertThat(tradutor.traduzir(evento("evt_1", "PAYMENT_QUALQUER_COISA", "10", null))).isEmpty();
    }

    @Test
    void referenciaQueNaoEhIdDePedidoDeveSerIgnoradaSemLancarExcecao() {
        // Antes: NumberFormatException depois de registrar o evento -> reprocessamento sem fim
        Optional<EventoPagamento> traduzido = tradutor.traduzir(evento("evt_1", "PAYMENT_CONFIRMED", "assinatura-42", null));

        assertThat(traduzido).isEmpty();
    }
}
