package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.FreteServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreteServiceConfig {
    @Bean
    public FreteService freteService(TokenService tokenService, ProdutoService produtoService) {
        return new FreteServiceImpl(tokenService, produtoService);
    }
}
