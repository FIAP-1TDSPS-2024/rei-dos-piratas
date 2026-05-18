package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.frete.JpaTokenMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaTokenEntityRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenRepositoryImpl implements TokenRepository {

    private final JpaTokenEntityRepository repository;

    public TokenRepositoryImpl(JpaTokenEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Token save(Token token) {
        log.debug("[REPO-TOKEN] Salvando novo token OAuth");
        Token salvo = JpaTokenMapper.toEntity(
                this.repository.save(JpaTokenMapper.toJpaEntity(token)));
        log.info("[REPO-TOKEN] Token OAuth salvo com sucesso - ID={}", salvo.getId());
        return salvo;
    }

    public Token findLastToken() {
        log.debug("[REPO-TOKEN] Buscando último token OAuth persistido");
        Token token = JpaTokenMapper.toEntity(this.repository.findLastToken());
        if (token == null) {
            log.warn("[REPO-TOKEN] Nenhum token OAuth encontrado no banco");
            return null;
        }
        log.debug("[REPO-TOKEN] Token OAuth encontrado - válido: {}", token.isTokenValid());
        return token;
    }
}
