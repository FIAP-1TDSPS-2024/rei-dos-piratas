package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.MotivoDevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.MotivoDevolucaoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.MotivoDevolucaoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MotivoDevolucaoServiceConfig {

    @Bean
    public MotivoDevolucaoService motivoDevolucaoService(MotivoDevolucaoRepository motivoDevolucaoRepository) {
        return new MotivoDevolucaoServiceImpl(motivoDevolucaoRepository);
    }
}

