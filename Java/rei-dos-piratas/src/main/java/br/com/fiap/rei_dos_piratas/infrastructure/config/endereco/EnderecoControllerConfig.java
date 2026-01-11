package br.com.fiap.rei_dos_piratas.infrastructure.config.endereco;

import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.EnderecoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.EnderecoControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnderecoControllerConfig {
    @Bean
    public EnderecoController enderecoController(EnderecoService service) {
        return new EnderecoControllerImpl(service);
    }
}
