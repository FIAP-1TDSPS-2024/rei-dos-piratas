package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.FreteAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.GatewayFreteMelhorEnvio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayFreteConfig {

    @Bean
    public GatewayFrete gatewayFrete(FreteAppClient freteAppClient) {
        return new GatewayFreteMelhorEnvio(freteAppClient);
    }
}
