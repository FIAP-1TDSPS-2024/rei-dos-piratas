package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record VendedorInDto(
        @NotNull(message = "O nome não pode ser nulo")
        @Length(max=30, message = "O nome de usuário deve ter de 3 a 30 caracteres")
        String userName,

        @NotNull(message = "O nome completo não deve estar nulo")
        @Length(min=5, max=50, message = "O nome completo usuário deve ter de 5 a 50 caracteres")
        String nomeCompleto,

        @Email(message = "Insira um e-mail válido")
        @Length(max = 40, message = "O e-mail deve ter até 20 caracteres")
        String email,

        @NotNull(message = "A senha não pode ser nula")
        @Length(min=8, max = 20, message = "A senha deve possuir de 8 a 20 caracteres")
        String senha,

        Role role
) {
}
