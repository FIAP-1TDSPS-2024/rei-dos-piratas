package br.com.fiap.rei_dos_piratas.infrastructure.config.carrinho;

import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.CarrinhoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.PedidoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCarrinhoEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPedidoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarrinhoRepositoryConfig {
    @Bean
    public CarrinhoRepository carrinhoRepository(JpaCarrinhoEntityRepository jpaCarrinhoEntityRepository) {
        return new CarrinhoRepositoryImpl(jpaCarrinhoEntityRepository);
    }
}
