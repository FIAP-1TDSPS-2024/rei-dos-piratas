package br.com.fiap.rei_dos_piratas.domain.exceptions;

public class UniqueKeyDuplicatedException extends RuntimeException {
    public UniqueKeyDuplicatedException(String message) {
        super(message);
    }
}
