package br.com.fiap.rei_dos_piratas.infrastructure.config.rastreio;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.application.service.impl.RastreioServiceImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RastreioServiceConfig {

    @Bean
    public RastreioService rastreioService(HmacUtil hmacUtil,
                                           ObjectMapper objectMapper,
                                           PedidoService pedidoService,
                                           DevolucaoService devolucaoService) {
        return new RastreioServiceImpl(hmacUtil, objectMapper, pedidoService, devolucaoService);
    }
}

