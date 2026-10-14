package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.application.service.EventoExternoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.Enum.EventoAsaasEnum;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class RastreioCobrancaServiceImpl implements RastreioCobrancaService {

    private final PedidoService pedidoService;
    private final EstornoService estornoService;

    private final EventoExternoService eventoExternoService;

    public RastreioCobrancaServiceImpl(PedidoService pedidoService,
                                       EstornoService estornoService,
                                       EventoExternoService eventoExternoService) {
        this.pedidoService = pedidoService;
        this.estornoService = estornoService;
        this.eventoExternoService = eventoExternoService;
    }

    @Override
    public void processarEvento(EventoCobrancaDto evento, String payload) {

        if (evento == null) {
            log.warn("Evento de cobrança nulo ignorado");
            return;
        }

        if (evento.id() == null || evento.id().isBlank()) {
            log.warn("Evento de cobrança sem ID ignorado: event={}", evento.event());
            return;
        }

        if (evento.event() == null ||
            evento.payment() == null ||
            evento.payment().externalReference() == null) {
            log.warn("Não foi possível encontrar um pedido associado ao evento de cobrança ID: {}", evento.id());
            return;
        }

        EventoAsaasEnum tipoEvento;
        try {
            tipoEvento = EventoAsaasEnum.valueOf(evento.event());
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de evento desconhecido ignorado: ID={}, event={}", evento.id(), evento.event());
            return;
        }

        Optional<EventoExterno> registro = eventoExternoService.iniciarProcessamento(
                OrigemEventoEnum.ASAAS, evento.id(), tipoEvento.name(), payload);
        if (registro.isEmpty()) {
            return;
        }

        try {
            Long pedidoId = Long.valueOf(evento.payment().externalReference());

            if (tipoEvento.isCobranca()) {
                log.info("Processando evento de cobrança: event={}, externalReference={}", evento.event(), evento.payment().externalReference());
                pedidoService.processarEventoCobranca(pedidoId, evento);
            } else {
                log.info("Processando evento de estorno: event={}, externalReference={}", evento.event(), evento.payment().externalReference());
                estornoService.processarEventoEstorno(pedidoId, evento);
            }

            eventoExternoService.concluir(registro.get().getId());
        } catch (RuntimeException e) {
            log.error("Falha ao processar evento Asaas ID={}", evento.id(), e);
            eventoExternoService.falhar(registro.get().getId());
            throw e;
        }
    }
}
