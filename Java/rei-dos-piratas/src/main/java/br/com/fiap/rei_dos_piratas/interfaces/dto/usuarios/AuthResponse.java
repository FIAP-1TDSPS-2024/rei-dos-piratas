package br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios;

import java.util.List;

public record AuthResponse(String token,
                           ClienteOutDto cliente,
                           FuncionarioOutDto funcionario,
                           List<String> roles) {

    private final static String TYPE = "Bearer";

    public String type() {
        return TYPE;
    }
}
