package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook;

public record RastreioWebhookDto(
        String event,
        RastreioDataDto data
) {
}
