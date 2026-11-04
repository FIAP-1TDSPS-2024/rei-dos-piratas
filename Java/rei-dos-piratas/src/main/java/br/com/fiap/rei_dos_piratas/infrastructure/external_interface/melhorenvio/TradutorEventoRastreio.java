package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.RastreioDataDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto.RastreioWebhookDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * Traduz o corpo do webhook de rastreio do Melhor Envio em {@link EventoRastreio}.
 * Vazio (com log de aviso) quando o corpo não é JSON válido ou não identifica um envio.
 * Nunca loga o corpo: ele traz nome e endereço do cliente.
 */
@Slf4j
public class TradutorEventoRastreio {

    /** Datas de negócio no fuso da loja; o Jackson entrega o OffsetDateTime em UTC. */
    private static final ZoneId FUSO_LOJA = ZoneId.of("America/Sao_Paulo");

    private final ObjectMapper objectMapper;

    public TradutorEventoRastreio(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<EventoRastreio> traduzir(String corpo) {
        RastreioWebhookDto webhook;
        try {
            webhook = this.objectMapper.readValue(corpo, RastreioWebhookDto.class);
        } catch (JsonProcessingException e) {
            log.warn("[MELHOR-ENVIO-RASTREIO] Webhook com corpo inválido ignorado");
            return Optional.empty();
        }

        RastreioDataDto data = webhook.data();
        if (data == null || data.id() == null || data.id().isBlank()) {
            log.warn("[MELHOR-ENVIO-RASTREIO] Webhook sem identificador do envio ignorado: event={}", webhook.event());
            return Optional.empty();
        }

        UUID envio;
        try {
            envio = UUID.fromString(data.id());
        } catch (IllegalArgumentException e) {
            log.warn("[MELHOR-ENVIO-RASTREIO] Webhook com identificador de envio inválido ignorado: event={}", webhook.event());
            return Optional.empty();
        }

        LocalDate dataEntrega = data.deliveredAt() == null ? null
                : data.deliveredAt().atZoneSameInstant(FUSO_LOJA).toLocalDate();

        return Optional.of(new EventoRastreio(
                tipoDoDominio(webhook.event()),
                webhook.event(),
                envio,
                data.status(),
                data.protocol(),
                data.tracking(),
                data.trackingUrl(),
                dataEntrega));
    }

    private TipoEventoRastreio tipoDoDominio(String evento) {
        if (evento == null) {
            return TipoEventoRastreio.OUTRO;
        }
        return switch (evento) {
            case "order.created" -> TipoEventoRastreio.CRIADO;
            case "order.released" -> TipoEventoRastreio.PAGO;
            case "order.generated", "order.ready-to-print" -> TipoEventoRastreio.ETIQUETA_GERADA;
            case "order.received" -> TipoEventoRastreio.RECEBIDO_PONTO;
            case "order.posted" -> TipoEventoRastreio.POSTADO;
            case "order.delivered" -> TipoEventoRastreio.ENTREGUE;
            case "order.undelivered" -> TipoEventoRastreio.NAO_ENTREGUE;
            case "order.paused" -> TipoEventoRastreio.PAUSADO;
            case "order.suspended" -> TipoEventoRastreio.SUSPENSO;
            case "order.canceled" -> TipoEventoRastreio.CANCELADO;
            case "order.expired" -> TipoEventoRastreio.EXPIRADO;
            default -> TipoEventoRastreio.OUTRO;
        };
    }
}
