package br.com.fiap.rei_dos_piratas.infrastructure.config.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Configuração do client da API de cobrança (Asaas).
 * Propositalmente sem @Configuration: assim os beans ficam restritos ao
 * CobrancaAppClient e não vazam para os demais clients Feign.
 * A autenticação via access-token é feita por header em application.properties.
 */
public class CobrancaFeignConfig {

    @Bean
    public ErrorDecoder asaasErrorDecoder() {
        return new AsaasErrorDecoder();
    }
}
