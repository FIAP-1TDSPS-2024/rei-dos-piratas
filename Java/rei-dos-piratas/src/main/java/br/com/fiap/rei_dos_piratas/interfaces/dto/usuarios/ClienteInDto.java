package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;

import java.time.LocalDate;

/** DTO de entrada — apenas transporta dados, sem anotações de validação. */
public record ClienteInDto(
        String userName,
        String nomeCompleto,
        String email,
        String senha,
        LocalDate dataNascimento,
        SexoEnum sexo,
        String cpf,
        String celular
) {}
