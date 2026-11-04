package br.com.fiap.rei_dos_piratas.domain.exceptions;

/** Webhook com assinatura ausente ou que não confere (origem não comprovada). Responde 401. */
public class AssinaturaInvalidaException extends RuntimeException {
    public AssinaturaInvalidaException(String message) {
        super(message);
    }
}
