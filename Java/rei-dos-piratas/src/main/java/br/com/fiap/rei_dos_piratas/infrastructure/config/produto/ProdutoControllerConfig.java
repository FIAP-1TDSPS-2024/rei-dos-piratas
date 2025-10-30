package br.com.fiap.rei_dos_piratas.infrastructure.config.produto;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.ClienteControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.FuncionarioControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.ProdutoControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoControllerConfig {
    @Bean
    public ProdutoController produtoController(ProdutoService service) {
        return new ProdutoControllerImpl(service);
    }
}
