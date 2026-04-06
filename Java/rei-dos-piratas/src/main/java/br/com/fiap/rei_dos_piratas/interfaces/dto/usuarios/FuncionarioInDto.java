package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.PerfilEnum;

import java.math.BigDecimal;

/** DTO de entrada — apenas transporta dados, sem anotações de validação. */
public record FuncionarioInDto(
        String userName,
        String nomeCompleto,
        String email,
        String senha,
        PerfilEnum perfil,
        BigDecimal salario
) {}
