package br.com.fiap.rei_dos_piratas.infrastructure.config.produto;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.FuncionarioServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.ProdutoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoServiceConfig {

    @Bean
    public ProdutoService produtoService(ProdutoRepository repository) {
        return new ProdutoServiceImpl(repository);
    }


}
