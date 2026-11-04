package br.com.fiap.rei_dos_piratas.domain.gateway;

/** Resultado da tokenização: só o token, os 4 últimos dígitos e a bandeira. */
public record CartaoTokenizado(String token, String ultimosDigitos, String bandeira) {

    @Override
    public String toString() {
        // Sem token/dígitos/bandeira: evita vazamento em logs
        return "CartaoTokenizado[***]";
    }
}
