package br.com.fiap.rei_dos_piratas.infrastructure.config.frete;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FreteController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.CarrinhoControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.FreteControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreteControllerConfig {
    @Bean
    public FreteController freteController(FreteService freteService,ProdutoService produtoService) {
        return new FreteControllerImpl(freteService, produtoService);
    }
}
