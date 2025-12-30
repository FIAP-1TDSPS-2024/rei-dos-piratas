package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.application.service.impl.FreteServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.TokenRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaTokenEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRepositoryConfig {
    @Bean
    public TokenRepository tokenRepository(JpaTokenEntityRepository repository) {
        return new TokenRepositoryImpl(repository);
    }
}
