package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.PedidoServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.ProdutoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoServiceConfig {

    @Bean
    public PedidoService pedidoService(PedidoRepository repository, ProdutoRepository produtoRepository, EnderecoService enderecoService, DadosEmpresaRepository dadosEmpresaRepository, FreteService freteService, HmacUtil hmacUtil, ObjectMapper objectMapper) {
        return new PedidoServiceImpl(repository, produtoRepository, enderecoService, dadosEmpresaRepository, freteService, hmacUtil, objectMapper);
    }


}
