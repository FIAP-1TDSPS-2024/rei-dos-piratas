package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventoPagamentoTest {

    private EventoPagamento recusa(String motivo) {
        return new EventoPagamento("evt_1", TipoEventoPagamento.RECUSADA, "PAYMENT_CREDIT_CARD_CAPTURE_REFUSED",
                LocalDateTime.of(2026, 11, 4, 10, 0), "pay_1", 10L, motivo);
    }

    @Test
    void motivoRecusa_DeveUsarOMotivoDaOperadoraQuandoInformado() {
        assertThat(recusa("Saldo insuficiente").motivoRecusaOuPadrao()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void motivoRecusa_SemMotivoDaOperadoraDeveUsarTextoPadraoComOEvento() {
        assertThat(recusa(null).motivoRecusaOuPadrao())
                .isEqualTo("Pagamento recusado pela operadora (PAYMENT_CREDIT_CARD_CAPTURE_REFUSED)");
    }

    @ParameterizedTest
    @EnumSource(value = TipoEventoPagamento.class,
            names = {"ESTORNADA", "ESTORNO_PARCIAL", "ESTORNO_EM_ANDAMENTO", "ESTORNO_NEGADO", "OUTRO_ESTORNO"})
    void tiposDeEstornoDevemSerEstorno(TipoEventoPagamento tipo) {
        assertThat(tipo.isEstorno()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TipoEventoPagamento.class,
            names = {"CRIADA", "AUTORIZADA", "CONFIRMADA", "RECEBIDA", "RECUSADA", "VENCIDA", "REMOVIDA", "OUTRO"})
    void tiposDeCobrancaNaoSaoEstorno(TipoEventoPagamento tipo) {
        assertThat(tipo.isEstorno()).isFalse();
    }
}
