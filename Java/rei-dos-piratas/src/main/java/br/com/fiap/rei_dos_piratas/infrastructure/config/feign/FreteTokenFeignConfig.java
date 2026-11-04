package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuração do client de token OAuth do Melhor Envio: só o decodificador de erros
 * (o endpoint de token não leva o Bearer do token anterior). Sem @Configuration para não virar global.
 */
public class FreteTokenFeignConfig {

    @Bean
    public ErrorDecoder melhorEnvioTokenErrorDecoder() {
        return new MelhorEnvioErrorDecoder();
    }
}
