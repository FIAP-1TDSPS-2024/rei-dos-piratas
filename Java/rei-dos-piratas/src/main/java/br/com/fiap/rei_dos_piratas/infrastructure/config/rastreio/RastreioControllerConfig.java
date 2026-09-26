package br.com.fiap.rei_dos_piratas.infrastructure.config.rastreio;

import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.RastreioController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.RastreioControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RastreioControllerConfig {

    @Bean
    public RastreioController rastreioController(RastreioService rastreioService) {
        return new RastreioControllerImpl(rastreioService);
    }
}

