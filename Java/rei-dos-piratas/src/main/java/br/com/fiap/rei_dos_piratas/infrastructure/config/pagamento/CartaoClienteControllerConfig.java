package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CartaoClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.CartaoClienteControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartaoClienteControllerConfig {
    @Bean
    public CartaoClienteController cartaoClienteController(CartaoService service) {
        return new CartaoClienteControllerImpl(service);
    }
}
