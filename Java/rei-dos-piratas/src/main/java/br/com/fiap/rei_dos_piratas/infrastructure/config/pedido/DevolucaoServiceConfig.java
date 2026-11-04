package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import br.com.fiap.rei_dos_piratas.application.service.impl.DevolucaoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevolucaoServiceConfig {

    @Bean
    public DevolucaoService devolucaoService(DevolucaoRepository devolucaoRepository,
                                             GatewayFrete gatewayFrete,
                                             br.com.fiap.rei_dos_piratas.application.service.EstornoPagamentoService estornoPagamentoService,
                                             @Value("${ME_SERVICE_DEVOLUCAO:#{null}}") Long servicoEntregaDevolucao) {
        return new DevolucaoServiceImpl(devolucaoRepository, gatewayFrete, servicoEntregaDevolucao, estornoPagamentoService);
    }
}

