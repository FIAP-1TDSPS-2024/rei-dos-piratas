package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Token {
    private Long id;

    @NotNull(message = "O Token não pode ser nulo")
    private String token;

    @NotNull(message = "O refresh token não pode ser nulo")
    private String refreshToken;

    @PastOrPresent(message = "A data de criação do token deve estar no presente ou passado")
    private LocalDate dataCriacao;

    @NotNull(message = "A data de expiração do token não pode ser nula")
    private LocalDate dataExpiracao;

    public Token(String token, String refreshToken, LocalDate dataCriacao, int expiresInSeconds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.dataCriacao = dataCriacao;

        //Conversao de expiração em segundos para expiração em dias
        int expiresInDays = expiresInSeconds/86400;

        //Definição de data de expiração do Token
        this.dataExpiracao = dataCriacao.plusDays(expiresInDays);
    }
}
