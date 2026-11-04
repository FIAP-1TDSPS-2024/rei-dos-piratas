package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio;

import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Entrada do webhook de rastreio do Melhor Envio: valida a assinatura HMAC (header x-me-signature),
 * traduz o corpo e entrega o evento de domínio ao serviço.
 */
@Slf4j
public class WebhookRastreioMelhorEnvio {

    private final HmacUtil hmacUtil;
    private final TradutorEventoRastreio tradutor;
    private final RastreioService rastreioService;

    public WebhookRastreioMelhorEnvio(HmacUtil hmacUtil, TradutorEventoRastreio tradutor, RastreioService rastreioService) {
        this.hmacUtil = hmacUtil;
        this.tradutor = tradutor;
        this.rastreioService = rastreioService;
    }

    /**
     * @throws AssinaturaInvalidaException assinatura ausente ou que não confere (401)
     * Corpo sem envio identificável é ignorado (200); falha ao processar um evento válido é propagada (500).
     */
    public void receber(String assinatura, String corpo) {
        if (!assinaturaConfere(assinatura, corpo)) {
            log.warn("[MELHOR-ENVIO-RASTREIO] Webhook rejeitado — assinatura HMAC ausente ou inválida");
            throw new AssinaturaInvalidaException("Assinatura do webhook inválida");
        }

        this.tradutor.traduzir(corpo).ifPresent(this.rastreioService::processarEvento);
    }

    /** Comparação em tempo constante: {@code equals} encerra no primeiro byte diferente e vaza tempo. */
    private boolean assinaturaConfere(String assinatura, String corpo) {
        if (assinatura == null || corpo == null) {
            return false;
        }
        byte[] esperada = this.hmacUtil.generateHmac(corpo).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperada, assinatura.getBytes(StandardCharsets.UTF_8));
    }
}
