package br.com.fiap.rei_dos_piratas.domain.exceptions;

public class WrongStatusException extends RuntimeException {
    public WrongStatusException(String message) {
        super(message);
    }
}
