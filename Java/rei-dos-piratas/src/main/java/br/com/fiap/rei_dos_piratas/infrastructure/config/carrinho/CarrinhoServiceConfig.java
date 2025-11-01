package br.com.fiap.rei_dos_piratas.infrastructure.config.carrinho;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.CarrinhoServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.PedidoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarrinhoServiceConfig {
    @Bean
    public CarrinhoService carrinhoService(CarrinhoRepository carrinhoRepository, PedidoService pedidoService, ProdutoService produtoService, ClienteService clienteService) {
        return new CarrinhoServiceImpl(carrinhoRepository, pedidoService, produtoService, clienteService);
    }
}
