package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(@com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
                             @NotBlank @Size(max = 128) String refreshToken) {}
