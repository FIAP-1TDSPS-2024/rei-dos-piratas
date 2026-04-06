package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO de saída — apenas transporta dados, sem anotações de validação. */
public record FuncionarioOutDto(
        Long id,
        String userName,
        String nomeCompleto,
        String email,
        boolean usuarioAtivo,
        LocalDate dataCadastro,
        Perfil role,
        LocalDate dataDemissao,
        BigDecimal salario
) {}
