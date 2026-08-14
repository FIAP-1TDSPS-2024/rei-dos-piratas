package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.DevolucaoControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevolucaoControllerConfig {

    @Bean
    public DevolucaoController devolucaoController(DevolucaoService devolucaoService, PedidoService pedidoService) {
        return new DevolucaoControllerImpl(devolucaoService, pedidoService);
    }
}

