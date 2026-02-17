package br.com.fiap.rei_dos_piratas.infrastructure.config.carrinho;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.CarrinhoControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.PedidoControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarrinhoControllerConfig {
    @Bean
    public CarrinhoController carrinhoController(CarrinhoService carrinhoService, ProdutoService produtoService, EnderecoService enderecoService) {
        return new CarrinhoControllerImpl(carrinhoService, produtoService, enderecoService);
    }
}
