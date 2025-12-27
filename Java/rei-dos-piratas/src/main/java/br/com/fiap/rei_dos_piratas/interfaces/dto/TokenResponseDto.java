package br.com.fiap.rei_dos_piratas.interfaces.dto;

public record TokenResponseDto(
        String token_type,
        int expires_in,
        String access_token,
        String refresh_token
) {}
