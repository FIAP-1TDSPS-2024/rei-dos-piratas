package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;

public interface TokenService {
    Token findLastToken();
    Token gerarNovoToken(String refreshToken);
}
