package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

public record RastreioWebhookDto(
        String event,
        RastreioDataDto data
) {
}
