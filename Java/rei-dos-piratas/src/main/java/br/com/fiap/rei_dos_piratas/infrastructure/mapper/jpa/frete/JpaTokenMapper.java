package br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.frete;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.frete.JpaTokenEntity;

public class JpaTokenMapper {

    public static Token toEntity(JpaTokenEntity tokenEntity) {
        if (tokenEntity == null) return null;
        return new Token(
                tokenEntity.getId(),
                tokenEntity.getToken(),
                tokenEntity.getRefreshToken(),
                tokenEntity.getDataCriacao(),
                tokenEntity.getDataExpiracao());
    }

    public static JpaTokenEntity toJpaEntity(Token token) {
        return new JpaTokenEntity(
                token.getId(),
                token.getToken(),
                token.getRefreshToken(),
                token.getDataCriacao(),
                token.getDataExpiracao());
    }

    private JpaTokenMapper() {}
}
