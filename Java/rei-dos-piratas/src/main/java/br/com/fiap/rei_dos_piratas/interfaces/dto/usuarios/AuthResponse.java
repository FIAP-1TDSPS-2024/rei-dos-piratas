package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import java.util.List;

public record AuthResponse(String token,
                           ClienteOutDto cliente,
                           FuncionarioOutDto funcionario,
                           List<String> roles,
                           @com.fasterxml.jackson.annotation.JsonProperty("refresh_token") String refreshToken,
                           @com.fasterxml.jackson.annotation.JsonProperty("refresh_expira_em") java.time.Instant refreshExpiraEm) {

    private final static String TYPE = "Bearer";

    public String type() {
        return TYPE;
    }
}
