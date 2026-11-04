package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebhookRastreioMelhorEnvioTest {

    private static final String CORPO = """
            {"event":"order.posted","data":{"id":"123e4567-e89b-12d3-a456-426614174010","status":"posted"}}""";

    private RastreioService rastreioService;
    private WebhookRastreioMelhorEnvio webhook;

    @BeforeEach
    void setUp() {
        HmacUtil hmac = mock(HmacUtil.class);
        when(hmac.generateHmac(any())).thenReturn("assinatura-valida");
        rastreioService = mock(RastreioService.class);
        TradutorEventoRastreio tradutor = new TradutorEventoRastreio(new ObjectMapper().findAndRegisterModules()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE));
        webhook = new WebhookRastreioMelhorEnvio(hmac, tradutor, rastreioService);
    }

    @Test
    void assinaturaValidaDeveEntregarOEventoTraduzidoAoServico() {
        // Act
        webhook.receber("assinatura-valida", CORPO);

        // Assert
        ArgumentCaptor<EventoRastreio> captor = ArgumentCaptor.forClass(EventoRastreio.class);
        verify(rastreioService).processarEvento(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(TipoEventoRastreio.POSTADO);
        assertThat(captor.getValue().envio()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174010"));
    }

    @Test
    void assinaturaQueNaoConfereDeveSerRejeitadaSemProcessar() {
        assertThatThrownBy(() -> webhook.receber("assinatura-errada", CORPO))
                .isInstanceOf(AssinaturaInvalidaException.class);
        verifyNoInteractions(rastreioService);
    }

    @Test
    void assinaturaComTamanhoDiferenteDeveSerRejeitada() {
        assertThatThrownBy(() -> webhook.receber("assinatura-valida-com-sufixo", CORPO))
                .isInstanceOf(AssinaturaInvalidaException.class);
        verifyNoInteractions(rastreioService);
    }

    @Test
    void semAssinaturaDeveSerRejeitado() {
        assertThatThrownBy(() -> webhook.receber(null, CORPO))
                .isInstanceOf(AssinaturaInvalidaException.class);
        verifyNoInteractions(rastreioService);
    }

    @Test
    void corpoQueNaoIdentificaOEnvioDeveSerIgnoradoSemErro() {
        assertThatCode(() -> webhook.receber("assinatura-valida", "{\"event\":\"order.posted\"}"))
                .doesNotThrowAnyException();
        verifyNoInteractions(rastreioService);
    }

    @Test
    void falhaAoProcessarDeveSerPropagadaParaOFornecedorSaber() {
        // Arrange: ex.: banco fora; antes o erro era engolido e o evento se perdia
        doThrow(new IllegalStateException("banco fora")).when(rastreioService).processarEvento(any());

        // Act & Assert
        assertThatThrownBy(() -> webhook.receber("assinatura-valida", CORPO))
                .isInstanceOf(IllegalStateException.class);
    }
}
