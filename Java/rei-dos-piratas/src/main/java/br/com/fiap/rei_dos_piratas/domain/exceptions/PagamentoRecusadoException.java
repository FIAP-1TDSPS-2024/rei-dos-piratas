package br.com.fiap.rei_dos_piratas.domain.exceptions;

/** Pagamento recusado pela API de cobrança (ex.: cartão não autorizado). */
public class PagamentoRecusadoException extends RegraDeNegocioException {
    public PagamentoRecusadoException(String message) {
        super(message);
    }
}
