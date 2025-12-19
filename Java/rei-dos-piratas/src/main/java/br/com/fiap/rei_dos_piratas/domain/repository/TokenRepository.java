package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;

public interface TokenRepository {
    public Token save(Token token);
}
