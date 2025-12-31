package br.com.fiap.rei_dos_piratas.domain.exceptions;

public class ApiExternaException extends RuntimeException {
    public ApiExternaException(String message) {
        super(message);
    }
}
