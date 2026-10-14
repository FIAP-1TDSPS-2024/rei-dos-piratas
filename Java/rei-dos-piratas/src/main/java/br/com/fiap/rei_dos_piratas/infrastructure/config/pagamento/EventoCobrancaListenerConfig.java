package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.messaging.EventoCobrancaListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true")
public class EventoCobrancaListenerConfig {

    @Bean
    public EventoCobrancaListener eventoCobrancaListener(ObjectMapper objectMapper,
                                                         RastreioCobrancaService rastreioCobrancaService) {
        return new EventoCobrancaListener(objectMapper, rastreioCobrancaService);
    }
}
