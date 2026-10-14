package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.application.service.EventoExternoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.application.service.impl.EstornoServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.EventoExternoServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.RastreioCobrancaServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.EventoExternoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class EventoExternoServiceConfig {

    @Bean
    public EventoExternoService eventoExternoService(EventoExternoRepository eventoExternoRepository,
                                                     @Value("${app.eventos-externos.lease-segundos:300}") long leaseSegundos) {
        return new EventoExternoServiceImpl(eventoExternoRepository, Duration.ofSeconds(leaseSegundos));
    }

    @Bean
    public EstornoService estornoService(PedidoService pedidoService, DevolucaoService devolucaoService) {
        return new EstornoServiceImpl(pedidoService, devolucaoService);
    }

    @Bean
    public RastreioCobrancaService rastreioCobrancaService(PedidoService pedidoService,
                                                           EstornoService estornoService,
                                                           EventoExternoService eventoExternoService) {
        return new RastreioCobrancaServiceImpl(pedidoService, estornoService, eventoExternoService);
    }
}
