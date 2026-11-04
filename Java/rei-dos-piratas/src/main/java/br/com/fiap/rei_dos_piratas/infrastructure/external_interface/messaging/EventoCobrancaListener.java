package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.messaging;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.TradutorEventoAsaas;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.EventoCobrancaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

/** Recebe o webhook de cobrança do Asaas (via SQS), traduz para o domínio e repassa ao serviço. */
@Slf4j
public class EventoCobrancaListener implements EventoListener {

    private final ObjectMapper objectMapper;
    private final TradutorEventoAsaas tradutor;
    private final RastreioCobrancaService rastreioCobrancaService;

    public EventoCobrancaListener(ObjectMapper objectMapper, TradutorEventoAsaas tradutor, RastreioCobrancaService rastreioCobrancaService) {
        this.objectMapper = objectMapper;
        this.tradutor = tradutor;
        this.rastreioCobrancaService = rastreioCobrancaService;
    }

    @Override
    @SqsListener(queueNames = "${aws.sqs.queue-name}")
    public void onMessage(String message) {
        EventoCobrancaDto evento;
        try {
            evento = objectMapper.readValue(message, EventoCobrancaDto.class);
        } catch (JsonProcessingException e) {
            // Sem o payload no log: pode conter dados do cliente
            log.error("[ WEBHOOK COBRANCA ASAAS - SQS ] Mensagem inválida descartada: {}", e.getOriginalMessage());
            return;
        }

        log.info("[ WEBHOOK COBRANCA ASAAS - SQS ] Evento recebido: ID={}, evento={}", evento.id(), evento.event());
        tradutor.traduzir(evento)
                .ifPresent(eventoPagamento -> rastreioCobrancaService.processarEvento(eventoPagamento, message));
    }
}
