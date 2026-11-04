package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.EventoAsaasEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.EventoCobrancaDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Traduz o webhook de cobrança do Asaas em {@link EventoPagamento}.
 * Devolve vazio quando o evento deve ser ignorado: sem id, sem pedido associado ou de tipo desconhecido.
 * Nunca lança exceção por dado do fornecedor (a exceção faria o evento ser reprocessado sem fim).
 */
@Slf4j
public class TradutorEventoAsaas {

    public Optional<EventoPagamento> traduzir(EventoCobrancaDto evento) {
        if (evento == null) {
            log.warn("[ASAAS-EVENTO] Evento de cobrança nulo ignorado");
            return Optional.empty();
        }
        if (evento.id() == null || evento.id().isBlank()) {
            log.warn("[ASAAS-EVENTO] Evento de cobrança sem ID ignorado: event={}", evento.event());
            return Optional.empty();
        }
        if (evento.event() == null || evento.payment() == null || evento.payment().externalReference() == null) {
            log.warn("[ASAAS-EVENTO] Evento ID={} sem pedido associado ignorado", evento.id());
            return Optional.empty();
        }

        EventoAsaasEnum tipoAsaas;
        try {
            tipoAsaas = EventoAsaasEnum.valueOf(evento.event());
        } catch (IllegalArgumentException e) {
            log.warn("[ASAAS-EVENTO] Tipo de evento desconhecido ignorado: ID={}, event={}", evento.id(), evento.event());
            return Optional.empty();
        }

        Long pedidoId;
        try {
            pedidoId = Long.valueOf(evento.payment().externalReference());
        } catch (NumberFormatException e) {
            log.warn("[ASAAS-EVENTO] Evento ID={} com referência que não é ID de pedido ignorado", evento.id());
            return Optional.empty();
        }

        String motivoRecusa = evento.additionalInfo() != null ? evento.additionalInfo().denialReason() : null;

        return Optional.of(new EventoPagamento(
                evento.id(),
                tipoDoDominio(tipoAsaas),
                tipoAsaas.name(),
                evento.dateCreated(),
                evento.payment().id(),
                pedidoId,
                motivoRecusa));
    }

    private TipoEventoPagamento tipoDoDominio(EventoAsaasEnum tipo) {
        return switch (tipo) {
            case PAYMENT_CREATED -> TipoEventoPagamento.CRIADA;
            case PAYMENT_AUTHORIZED -> TipoEventoPagamento.AUTORIZADA;
            case PAYMENT_CONFIRMED -> TipoEventoPagamento.CONFIRMADA;
            case PAYMENT_RECEIVED -> TipoEventoPagamento.RECEBIDA;
            case PAYMENT_CREDIT_CARD_CAPTURE_REFUSED, PAYMENT_REPROVED_BY_RISK_ANALYSIS -> TipoEventoPagamento.RECUSADA;
            case PAYMENT_OVERDUE -> TipoEventoPagamento.VENCIDA;
            case PAYMENT_DELETED -> TipoEventoPagamento.REMOVIDA;
            case PAYMENT_AWAITING_RISK_ANALYSIS, PAYMENT_APPROVED_BY_RISK_ANALYSIS, PAYMENT_UPDATED,
                 PAYMENT_ANTICIPATED, PAYMENT_RESTORED -> TipoEventoPagamento.OUTRO;
            case PAYMENT_REFUNDED -> TipoEventoPagamento.ESTORNADA;
            case PAYMENT_PARTIALLY_REFUNDED -> TipoEventoPagamento.ESTORNO_PARCIAL;
            case PAYMENT_REFUND_IN_PROGRESS -> TipoEventoPagamento.ESTORNO_EM_ANDAMENTO;
            case PAYMENT_REFUND_DENIED -> TipoEventoPagamento.ESTORNO_NEGADO;
            case PAYMENT_RECEIVED_IN_CASH_UNDONE, PAYMENT_CHARGEBACK_REQUESTED, PAYMENT_CHARGEBACK_DISPUTE,
                 PAYMENT_AWAITING_CHARGEBACK_REVERSAL -> TipoEventoPagamento.OUTRO_ESTORNO;
        };
    }
}
