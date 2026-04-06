package br.com.fiap.rei_dos_piratas.infrastructure.config.produto;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ProdutoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    public ProdutoService produtoService(ProdutoRepository repository, Validator validator) {
        return new ProdutoServiceImpl(repository, validator);
    }
}

