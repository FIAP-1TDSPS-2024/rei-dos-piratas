package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.application.service.impl.PedidoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.entity.RegraBoleto;
import br.com.fiap.rei_dos_piratas.domain.entity.RegraParcelamento;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    public PedidoService pedidoService(PedidoRepository repository,
                                       ProdutoRepository produtoRepository,
                                       EnderecoService enderecoService,
                                       DadosEmpresaRepository dadosEmpresaRepository,
                                       FreteService freteService,
                                       ClienteService clienteService,
                                       GatewayPagamento gatewayPagamento,
                                       UsuarioAtualService usuarioAtual,
                                       CartaoService cartaoService,
                                       RegraParcelamento regraParcelamento,
                                       RegraBoleto regraBoleto) {
        return new PedidoServiceImpl(repository, produtoRepository, enderecoService, dadosEmpresaRepository, freteService, clienteService, gatewayPagamento, usuarioAtual,
                cartaoService, regraParcelamento, regraBoleto);
    }

}
