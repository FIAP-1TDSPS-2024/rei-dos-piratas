package br.com.fiap.rei_dos_piratas.infrastructure.config.rastreio;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.application.service.impl.RastreioServiceImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.TradutorEventoRastreio;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.WebhookRastreioMelhorEnvio;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RastreioServiceConfig {

    @Bean
    public RastreioService rastreioService(PedidoService pedidoService, DevolucaoService devolucaoService) {
        return new RastreioServiceImpl(pedidoService, devolucaoService);
    }

    @Bean
    public WebhookRastreioMelhorEnvio webhookRastreioMelhorEnvio(HmacUtil hmacUtil, ObjectMapper objectMapper,
                                                                 RastreioService rastreioService) {
        return new WebhookRastreioMelhorEnvio(hmacUtil, new TradutorEventoRastreio(objectMapper), rastreioService);
    }
}
