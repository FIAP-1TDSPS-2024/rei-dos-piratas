package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.Enum;

public enum EventoAsaasEnum {

    // Cobranças
    PAYMENT_CREATED,
    PAYMENT_AWAITING_RISK_ANALYSIS,
    PAYMENT_APPROVED_BY_RISK_ANALYSIS,
    PAYMENT_REPROVED_BY_RISK_ANALYSIS,
    PAYMENT_AUTHORIZED,
    PAYMENT_UPDATED,
    PAYMENT_CONFIRMED,
    PAYMENT_RECEIVED,
    PAYMENT_CREDIT_CARD_CAPTURE_REFUSED,
    PAYMENT_ANTICIPATED,
    PAYMENT_OVERDUE,
    PAYMENT_DELETED,
    PAYMENT_RESTORED,

    // Estornos e chargebacks
    PAYMENT_REFUNDED,
    PAYMENT_PARTIALLY_REFUNDED,
    PAYMENT_REFUND_IN_PROGRESS,
    PAYMENT_REFUND_DENIED,
    PAYMENT_RECEIVED_IN_CASH_UNDONE,
    PAYMENT_CHARGEBACK_REQUESTED,
    PAYMENT_CHARGEBACK_DISPUTE,
    PAYMENT_AWAITING_CHARGEBACK_REVERSAL;

    public boolean isEstorno() {
        return switch (this) {
            case PAYMENT_REFUNDED,
                 PAYMENT_PARTIALLY_REFUNDED,
                 PAYMENT_REFUND_IN_PROGRESS,
                 PAYMENT_REFUND_DENIED,
                 PAYMENT_RECEIVED_IN_CASH_UNDONE,
                 PAYMENT_CHARGEBACK_REQUESTED,
                 PAYMENT_CHARGEBACK_DISPUTE,
                 PAYMENT_AWAITING_CHARGEBACK_REVERSAL -> true;

            default -> false;
        };
    }

    public boolean isCobranca() {
        return !isEstorno();
    }
}
