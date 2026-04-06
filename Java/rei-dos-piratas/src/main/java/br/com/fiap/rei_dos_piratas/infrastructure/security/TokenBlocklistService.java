package br.com.fiap.rei_dos_piratas.infrastructure.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blocklist em memória de tokens JWT invalidados pelo logout.
 *
 * Cada entrada armazena a data de expiração original do token, permitindo
 * que o job de limpeza remova entradas que já expirariam de qualquer forma
 * — assim a estrutura não cresce indefinidamente.
 *
 * Nota: em ambientes com múltiplas instâncias, substitua por Redis.
 */
@Component
public class TokenBlocklistService {

    // jti ou token completo → data de expiração original
    private final ConcurrentHashMap<String, Date> blocklist = new ConcurrentHashMap<>();

    /** Invalida um token adicionando-o à blocklist. */
    public void invalidar(String token, Date expiracao) {
        blocklist.put(token, expiracao);
    }

    /** Retorna true se o token foi explicitamente invalidado por logout. */
    public boolean estaInvalidado(String token) {
        return blocklist.containsKey(token);
    }

    /**
     * Limpa entradas cujos tokens já teriam expirado de qualquer forma.
     * Roda a cada 30 minutos.
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void limparExpirados() {
        Date agora = new Date();
        blocklist.entrySet().removeIf(entry -> entry.getValue().before(agora));
    }
}

