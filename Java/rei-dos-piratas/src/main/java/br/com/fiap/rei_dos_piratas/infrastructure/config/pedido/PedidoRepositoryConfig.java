package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.PedidoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ProdutoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPedidoEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaProdutoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoRepositoryConfig {
    @Bean
    public PedidoRepository pedidoRepository(JpaPedidoEntityRepository jpaPedidoEntityRepository) {
        return new PedidoRepositoryImpl(jpaPedidoEntityRepository);
    }
}
