package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record EnderecoOutDto(

        Long id,

        @Digits(integer = 5, fraction = 0, message = "O número deve possuir até 5 dígitos")
        int numero,

        @Pattern(regexp = "[0-9]{8}", message = "O CEP deve ter 8 dígitos, sendo apenas números")
        String cep,

        @Length(max = 70, message = "O logradouro deve possuir até 70 algarismos")
        String logradouro,

        @Length(max = 50, message = "O bairro deve possuir até 50 caracteres")
        String bairro,

        Long cidadeId,

        @NotNull(message = "O nome da cidadeNome não pode ser nulo")
        @Length(max = 50, message = "O nome da cidadeNome deve possuir até 50 caracteres")
        String cidade,

        Long estadoId,

        @NotNull(message = "O nome do estado não pode ser nulo")
        @Length(max = 50, message = "O nome do estado deve possuir até 50 caracteres")
        String estadoNome,

        @NotNull(message = "A sigla do estado não pode estar nula")
        @Length(min = 2, max = 2, message = "A sigla do estado deve possuir 2 dígitos")
        String estadoSigla
) {}
