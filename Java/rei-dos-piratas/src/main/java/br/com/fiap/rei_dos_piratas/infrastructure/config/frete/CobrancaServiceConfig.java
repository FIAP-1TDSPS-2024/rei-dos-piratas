package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.CobrancaService;
import br.com.fiap.rei_dos_piratas.application.service.impl.CobrancaServiceImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CobrancaServiceConfig {
    @Bean
    public CobrancaService cobrancaService(CobrancaAppClient apiCobranca) {
        return new CobrancaServiceImpl(apiCobranca);
    }
}
