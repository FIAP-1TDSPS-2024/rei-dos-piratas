package br.com.fiap.rei_dos_piratas.domain.exceptions;

public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
