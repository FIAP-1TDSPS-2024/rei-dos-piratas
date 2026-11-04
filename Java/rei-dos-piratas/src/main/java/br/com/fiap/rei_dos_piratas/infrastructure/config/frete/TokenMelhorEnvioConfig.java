package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.domain.repository.TokenRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.CredenciaisMelhorEnvio;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.FreteTokenClient;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.ProvedorTokenMelhorEnvio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenMelhorEnvioConfig {

    @Bean
    public CredenciaisMelhorEnvio credenciaisMelhorEnvio(@Value("${melhorenvio.client-id}") String clientId,
                                                         @Value("${melhorenvio.client-secret}") String clientSecret,
                                                         @Value("${melhorenvio.redirect-uri}") String redirectUri) {
        return new CredenciaisMelhorEnvio(clientId, clientSecret, redirectUri);
    }

    @Bean
    public ProvedorTokenMelhorEnvio provedorTokenMelhorEnvio(TokenRepository repository, FreteTokenClient api,
                                                             CredenciaisMelhorEnvio credenciais) {
        return new ProvedorTokenMelhorEnvio(repository, api, credenciais);
    }
}
