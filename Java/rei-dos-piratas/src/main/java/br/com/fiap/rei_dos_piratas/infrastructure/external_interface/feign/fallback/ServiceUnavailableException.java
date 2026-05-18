package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.fallback;

/**
 * Lançada pelo fallback do Circuit Breaker quando um serviço externo está indisponível
 * (circuito OPEN) e a operação não pode ser degradada com segurança.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}

