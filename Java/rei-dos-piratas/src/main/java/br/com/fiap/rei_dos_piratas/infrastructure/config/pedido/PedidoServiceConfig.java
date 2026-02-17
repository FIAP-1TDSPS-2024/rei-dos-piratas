package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.PedidoServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.ProdutoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    public PedidoService pedidoService(PedidoRepository repository, ProdutoRepository produtoRepository, ClienteService clienteService, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService) {
        return new PedidoServiceImpl(repository, produtoRepository, clienteService, enderecoService, dadosEmpresaRepository, freteService);
    }


}
