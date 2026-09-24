package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.impl.DevolucaoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevolucaoServiceConfig {

    @Bean
    public DevolucaoService devolucaoService(DevolucaoRepository devolucaoRepository,
                                             FreteService freteService) {
        return new DevolucaoServiceImpl(devolucaoRepository, freteService);
    }
}

