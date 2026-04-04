package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FuncionarioOutDto(
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

        @PastOrPresent
        @NotNull(message = "A data de cadastro do usuário não pode ser nula")
        LocalDate dataCadastro,

        @NotNull(message = "O Perfil do vendedor não pode ser nulo na exibição para o usuário")
        Perfil role,

        @PastOrPresent(message = "A data de demissao deve ser passada")
        LocalDate dataDemissao,

        @Digits(fraction = 2, integer = 6, message = "O salario deve ter até 8 digitos com 2 dígitos após a vírgula")
        @DecimalMin(value = "0.0", message = "O salario não pode ser negativo")
        BigDecimal salario
) {}
