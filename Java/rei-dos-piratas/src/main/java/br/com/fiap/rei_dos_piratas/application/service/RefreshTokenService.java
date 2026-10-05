package br.com.fiap.rei_dos_piratas.application.service;

import java.util.UUID;

public interface RefreshTokenService {
    String gerar(UUID sessaoId);
    String hash(String token);
    UUID identificarSessao(String token);
}
