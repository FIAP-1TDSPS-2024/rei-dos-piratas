package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.application.service.impl.FreteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.TokenServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenServiceConfig {
    @Bean
    public TokenService tokenService(TokenRepository repository) {
        return new TokenServiceImpl(repository);
    }
}
