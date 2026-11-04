package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.GatewayPagamentoAsaas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayPagamentoConfig {

    @Bean
    public GatewayPagamento gatewayPagamento(CobrancaAppClient cobrancaAppClient) {
        return new GatewayPagamentoAsaas(cobrancaAppClient);
    }
}
