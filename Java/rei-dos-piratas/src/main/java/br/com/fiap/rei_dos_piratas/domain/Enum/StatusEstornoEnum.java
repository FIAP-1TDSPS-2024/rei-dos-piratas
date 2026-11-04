package br.com.fiap.rei_dos_piratas.domain.Enum;

public enum StatusEstornoEnum {
    /** Gravado antes de chamar o fornecedor. */
    SOLICITADO,
    EM_ANDAMENTO,
    /** Boleto: o cliente precisa informar os dados bancários no link do fornecedor. */
    AGUARDANDO_CLIENTE,
    CONCLUIDO,
    NEGADO,
    /** O fornecedor recusou ou não respondeu; precisa de ação manual. */
    FALHOU;

    public boolean emAberto() {
        return this == SOLICITADO || this == EM_ANDAMENTO || this == AGUARDANDO_CLIENTE;
    }
}
