package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.FreteServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.impl.client.CloseableHttpClient;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreteServiceConfig {
    @Bean
    public FreteService freteService(GatewayFrete gatewayFrete, EnderecoService enderecoService) {
        return new FreteServiceImpl(gatewayFrete, enderecoService);
    }
}
