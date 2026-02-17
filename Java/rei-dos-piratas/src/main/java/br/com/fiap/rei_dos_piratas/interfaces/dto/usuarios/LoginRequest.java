package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Insira um e-mail válido")
        String email,

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 3)
        String password
) {}
