package br.com.fiap.rei_dos_piratas.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP do comprador, exigido pelo Asaas no pagamento com cartão. Atrás do load balancer,
 * o endereço da conexão é o do balanceador: o IP original é o primeiro do X-Forwarded-For.
 */
public final class IpDoComprador {

    public static String de(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private IpDoComprador() {}
}
