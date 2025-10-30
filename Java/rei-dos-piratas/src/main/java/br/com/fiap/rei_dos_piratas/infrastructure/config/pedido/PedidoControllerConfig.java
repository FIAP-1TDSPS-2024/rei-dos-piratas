package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.PedidoControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.ProdutoControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoControllerConfig {
    @Bean
    public PedidoController pedidoController(PedidoService service, ClienteService clienteService) {
        return new PedidoControllerImpl(service, clienteService);
    }
}
