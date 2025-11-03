package br.com.fiap.rei_dos_piratas.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 100)
        String username,

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 3)
        String password
) {}
