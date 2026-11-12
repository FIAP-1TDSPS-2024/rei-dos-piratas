package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.melhorenvio.dto;

public record RastreioWebhookDto(
        String event,
        RastreioDataDto data
) {
}
