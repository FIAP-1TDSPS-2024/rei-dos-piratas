package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Date;

public record ClienteInDto(
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

        @Past
        @NotNull(message = "A data de nascimento não pode ser nula")
        LocalDate dataNascimento,

        @NotNull(message = "O sexo do usuário não pode ser nulo")
        SexoEnum sexo,

        @Valid
        @NotNull(message = "O endereço do usuário não pode ser nulo")
        Endereco endereco,

        @Pattern(regexp = "[0-9]{11}", message = "O CPF do usuário deve ter 11 dígitos, sendo apenas números")
        String cpf
) {}
