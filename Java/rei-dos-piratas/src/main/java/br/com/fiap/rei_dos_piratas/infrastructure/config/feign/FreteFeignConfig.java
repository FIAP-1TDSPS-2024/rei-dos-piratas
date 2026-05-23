package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.application.service.TokenService;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreteFeignConfig {

    @Bean
    public RequestInterceptor bearerAuthRequestInterceptor(TokenService tokenService) {
        return template -> {
            Token token = tokenService.findLastToken();
            if (token != null && token.getToken() != null && !token.getToken().isBlank()) {
                template.header("Authorization", "Bearer " + token.getToken());
            }
        };
    }

    @Bean
    public ErrorDecoder melhorEnvioErrorDecoder() {
        return new MelhorEnvioErrorDecoder();
    }
}
