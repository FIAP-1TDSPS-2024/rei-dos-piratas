package br.com.fiap.rei_dos_piratas.infrastructure.config.endereco;

import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayCep;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep.GatewayCepViaCep;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep.ViaCepClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayCepConfig {

    @Bean
    public GatewayCep gatewayCep(ViaCepClient viaCepClient) {
        return new GatewayCepViaCep(viaCepClient);
    }
}
