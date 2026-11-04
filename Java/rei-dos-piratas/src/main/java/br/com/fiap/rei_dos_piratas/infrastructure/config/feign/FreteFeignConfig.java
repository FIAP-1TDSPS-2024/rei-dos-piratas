package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.ProvedorTokenMelhorEnvio;
import br.com.fiap.rei_dos_piratas.domain.entity.Token;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuração do client da API do Melhor Envio (token OAuth + decodificador de erros).
 * Propositalmente sem @Configuration: com ela os beans viram globais e o token do Melhor Envio
 * era enviado também nas chamadas ao Asaas (e as cobranças dependiam do token de frete).
 */
public class FreteFeignConfig {

    @Bean
    public RequestInterceptor bearerAuthRequestInterceptor(ProvedorTokenMelhorEnvio provedorToken) {
        return template -> {
            Token token = provedorToken.tokenValido();
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
