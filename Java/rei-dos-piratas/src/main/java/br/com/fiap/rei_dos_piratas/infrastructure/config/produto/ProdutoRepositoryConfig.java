package br.com.fiap.rei_dos_piratas.infrastructure.config.produto;

import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ProdutoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaProdutoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoRepositoryConfig {

    @Bean
    public ProdutoRepository produtoRepository(JpaProdutoEntityRepository jpaProdutoEntityRepository) {
        return new ProdutoRepositoryImpl(jpaProdutoEntityRepository);
    }
}
