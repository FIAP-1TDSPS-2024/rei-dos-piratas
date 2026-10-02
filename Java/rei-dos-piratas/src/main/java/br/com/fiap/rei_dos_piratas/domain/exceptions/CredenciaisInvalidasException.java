package br.com.fiap.rei_dos_piratas.domain.exceptions;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Usuario e/ou senha invalidos");
    }
}
