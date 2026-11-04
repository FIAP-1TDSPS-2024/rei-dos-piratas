package br.com.fiap.rei_dos_piratas.domain.Enum;

/** Tipo de evento de pagamento, independente do fornecedor. */
public enum TipoEventoPagamento {
    CRIADA,
    AUTORIZADA,
    CONFIRMADA,
    RECEBIDA,
    RECUSADA,
    VENCIDA,
    REMOVIDA,
    /** Evento de cobrança sem efeito no pedido (ex.: atualização, análise de risco em andamento). */
    OUTRO,
    ESTORNADA,
    ESTORNO_PARCIAL,
    ESTORNO_EM_ANDAMENTO,
    ESTORNO_NEGADO,
    /** Chargeback e afins: hoje só registrados em log. */
    OUTRO_ESTORNO;

    public boolean isEstorno() {
        return switch (this) {
            case ESTORNADA, ESTORNO_PARCIAL, ESTORNO_EM_ANDAMENTO, ESTORNO_NEGADO, OUTRO_ESTORNO -> true;
            default -> false;
        };
    }
}
