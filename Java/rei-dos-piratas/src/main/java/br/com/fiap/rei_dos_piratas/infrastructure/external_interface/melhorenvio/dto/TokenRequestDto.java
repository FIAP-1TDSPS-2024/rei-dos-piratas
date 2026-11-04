package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

public record TokenRequestDto(
        String grant_type,
        String client_id,
        String client_secret,
        String redirect_uri,
        String refresh_token
)
{}
