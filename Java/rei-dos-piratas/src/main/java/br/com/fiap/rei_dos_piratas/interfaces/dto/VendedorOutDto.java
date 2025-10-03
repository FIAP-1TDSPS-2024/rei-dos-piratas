package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record VendedorOutDto(
        @NotNull(message = "O ID não deve estar nulo para exibição ao usuário")
        Long id,

        @NotNull(message = "O nome não pode ser nulo")
        @Length(max=30, message = "O nome de usuário deve ter de 3 a 30 caracteres")
        String userName,

        @NotNull(message = "O nome completo não deve estar nulo")
        @Length(min=5, max=50, message = "O nome completo usuário deve ter de 5 a 50 caracteres")
        String nomeCompleto,

        @Email(message = "Insira um e-mail válido")
        @Length(max = 40, message = "O e-mail deve ter até 20 caracteres")
        String email,

        boolean usuarioAtivo,

        @Past
        @NotNull(message = "A data de cadastro do usuário não pode ser nula")
        LocalDate dataCadastro,

        @NotNull(message = "O Role do vendedor não pode ser nulo na exibição para o usuário")
        Role role
) {}
