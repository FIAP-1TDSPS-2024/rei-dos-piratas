package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuração só do client do ViaCEP. Propositalmente sem @Configuration, para o decoder não
 * virar global (ver FreteFeignConfig).
 */
public class ViaCepFeignConfig {

    @Bean
    public ErrorDecoder viaCepErrorDecoder() {
        return (metodo, resposta) -> new ApiExternaException(
                "Não foi possível consultar o CEP agora. Tente novamente em instantes.");
    }
}
