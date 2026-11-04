package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TradutorEventoRastreioTest {

    private static final String ENVIO = "123e4567-e89b-12d3-a456-426614174010";

    // Mesmo naming global do projeto (snake_case)
    private final TradutorEventoRastreio tradutor = new TradutorEventoRastreio(new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE));

    private String corpo(String evento, String id, String entregueEm) {
        return """
                {"event":"%s","data":{"id":%s,"protocol":"ORD-1","status":"delivered","tracking":"BR123",
                 "tracking_url":"https://rastreio/BR123","delivered_at":%s}}"""
                .formatted(evento, id == null ? "null" : "\"" + id + "\"", entregueEm == null ? "null" : "\"" + entregueEm + "\"");
    }

    @Test
    void deveTraduzirOsDadosDoEnvio() {
        // Act
        EventoRastreio evento = tradutor.traduzir(corpo("order.delivered", ENVIO, null)).orElseThrow();

        // Assert
        assertThat(evento.tipo()).isEqualTo(TipoEventoRastreio.ENTREGUE);
        assertThat(evento.tipoOriginal()).isEqualTo("order.delivered");
        assertThat(evento.envio()).isEqualTo(UUID.fromString(ENVIO));
        assertThat(evento.statusEnvio()).isEqualTo("delivered");
        assertThat(evento.protocolo()).isEqualTo("ORD-1");
        assertThat(evento.codigoRastreio()).isEqualTo("BR123");
        assertThat(evento.urlRastreio()).isEqualTo("https://rastreio/BR123");
        assertThat(evento.dataEntrega()).isNull();
    }

    @Test
    void entregaDepoisDas21hDeBrasiliaDeveFicarNoDiaDaLoja() {
        // 23:30 em Brasília = 02:30 do dia seguinte em UTC
        EventoRastreio evento = tradutor.traduzir(corpo("order.delivered", ENVIO, "2026-10-24T02:30:00Z")).orElseThrow();

        assertThat(evento.dataEntrega()).isEqualTo(LocalDate.of(2026, 10, 23));
    }

    @ParameterizedTest
    @CsvSource({
            "order.created, CRIADO",
            "order.released, PAGO",
            "order.generated, ETIQUETA_GERADA",
            "order.ready-to-print, ETIQUETA_GERADA",
            "order.received, RECEBIDO_PONTO",
            "order.posted, POSTADO",
            "order.delivered, ENTREGUE",
            "order.undelivered, NAO_ENTREGUE",
            "order.paused, PAUSADO",
            "order.suspended, SUSPENSO",
            "order.canceled, CANCELADO",
            "order.expired, EXPIRADO",
            "order.qualquer-outro, OUTRO"
    })
    void deveMapearCadaEventoDoMelhorEnvio(String eventoMelhorEnvio, TipoEventoRastreio esperado) {
        assertThat(tradutor.traduzir(corpo(eventoMelhorEnvio, ENVIO, null)))
                .map(EventoRastreio::tipo)
                .contains(esperado);
    }

    @Test
    void semIdDoEnvioDeveSerIgnorado() {
        assertThat(tradutor.traduzir(corpo("order.posted", null, null))).isEmpty();
    }

    @Test
    void idDoEnvioQueNaoEhUuidDeveSerIgnorado() {
        assertThat(tradutor.traduzir(corpo("order.posted", "nao-e-uuid", null))).isEmpty();
    }

    @Test
    void semDadosDoEnvioDeveSerIgnorado() {
        assertThat(tradutor.traduzir("{\"event\":\"order.posted\"}")).isEmpty();
    }

    @Test
    void corpoQueNaoEhJsonDeveSerIgnoradoSemLancarExcecao() {
        assertThat(tradutor.traduzir("isto não é json")).isEmpty();
    }
}
