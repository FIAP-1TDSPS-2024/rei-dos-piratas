package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

public record TokenResponseDto(
        String token_type,
        int expires_in,
        String access_token,
        String refresh_token
) {}
