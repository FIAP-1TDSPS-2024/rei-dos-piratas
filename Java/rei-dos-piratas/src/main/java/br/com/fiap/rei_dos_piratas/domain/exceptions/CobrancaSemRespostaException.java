package br.com.fiap.rei_dos_piratas.domain.exceptions;

/**
 * O fornecedor de pagamento não respondeu (timeout/conexão): não se sabe se a cobrança foi criada.
 * Não deve ser repetida automaticamente (risco de cobrança duplicada); o evento de pagamento resolve.
 */
public class CobrancaSemRespostaException extends ApiExternaException {
    public CobrancaSemRespostaException(String message) {
        super(message);
    }
}
