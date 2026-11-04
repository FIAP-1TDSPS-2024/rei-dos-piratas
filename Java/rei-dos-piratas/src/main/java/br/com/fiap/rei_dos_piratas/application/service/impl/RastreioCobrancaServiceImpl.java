package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.application.service.EventoExternoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
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

    /** O evento chega já traduzido e validado pelo adaptador do fornecedor (sem id/pedido é descartado lá). */
    @Override
    public void processarEvento(EventoPagamento evento, String payload) {

        if (evento == null) {
            log.warn("Evento de cobrança nulo ignorado");
            return;
        }

        Optional<EventoExterno> registro = eventoExternoService.iniciarProcessamento(
                OrigemEventoEnum.ASAAS, evento.idEvento(), evento.tipoOriginal(), payload);
        if (registro.isEmpty()) {
            return;
        }

        try {
            if (evento.tipo().isEstorno()) {
                log.info("Processando evento de estorno: event={}, pedido ID={}", evento.tipoOriginal(), evento.pedidoId());
                estornoService.processarEventoEstorno(evento.pedidoId(), evento);
            } else {
                log.info("Processando evento de cobrança: event={}, pedido ID={}", evento.tipoOriginal(), evento.pedidoId());
                pedidoService.processarEventoCobranca(evento.pedidoId(), evento);
            }

            eventoExternoService.concluir(registro.get().getId());
        } catch (RuntimeException e) {
            log.error("Falha ao processar evento Asaas ID={}", evento.idEvento(), e);
            eventoExternoService.falhar(registro.get().getId());
            throw e;
        }
    }
}
