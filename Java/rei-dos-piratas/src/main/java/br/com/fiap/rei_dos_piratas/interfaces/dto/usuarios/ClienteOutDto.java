package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Carrinho;

import java.time.LocalDate;

/** DTO de saída — apenas transporta dados, sem anotações de validação. */
public record ClienteOutDto(
        Long id,
        String userName,
        String nomeCompleto,
        String email,
        String celular,
        boolean usuarioAtivo,
        LocalDate dataCadastro,
        LocalDate dataNascimento,
        SexoEnum sexo,
        Carrinho carrinho
) {}
