package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.messaging;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventoCobrancaListener implements EventoListener{

    private final ObjectMapper objectMapper;
    private final RastreioCobrancaService rastreioCobrancaService;

    public EventoCobrancaListener(ObjectMapper objectMapper, RastreioCobrancaService rastreioCobrancaService) {
        this.objectMapper = objectMapper;
        this.rastreioCobrancaService = rastreioCobrancaService;
    }

    @Override
    @SqsListener(queueNames = "${aws.sqs.queue-name}")
    public void onMessage(String message) {
        try {
            EventoCobrancaDto evento = deserializar(message);
            rastreioCobrancaService.processarEvento(evento, message);
        } catch (JsonProcessingException e) {
            log.error("[ WEBHOOK COBRANCA ASAAS - SQS ] Erro ao deserializar a mensagem: {}", message, e);
        }

    }

    private EventoCobrancaDto deserializar(String message) throws JsonProcessingException {
        EventoCobrancaDto evento = objectMapper.readValue(message, EventoCobrancaDto.class);

        log.info("[ WEBHOOK COBRANCA ASAAS - SQS ] Evento recebido: ID: {}, Evento: {}",
                evento.id(),
                evento
        );;

        return evento;
    }
}
