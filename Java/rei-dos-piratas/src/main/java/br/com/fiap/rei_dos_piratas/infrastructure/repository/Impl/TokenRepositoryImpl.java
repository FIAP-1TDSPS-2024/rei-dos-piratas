package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaProdutoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaTokenMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaTokenEntityRepository;

public class TokenRepositoryImpl implements TokenRepository {

    private final JpaTokenEntityRepository repository;

    public TokenRepositoryImpl(JpaTokenEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Token save(Token token) {
        return JpaTokenMapper.toEntity(
                this.repository.save(
                        JpaTokenMapper.toJpaEntity(token)));
    }

    public Token findLastToken() {
        return JpaTokenMapper.toEntity(
                this.repository.findLastToken());
    }
}
