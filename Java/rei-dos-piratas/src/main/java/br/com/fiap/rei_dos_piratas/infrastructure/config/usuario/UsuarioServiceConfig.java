package br.com.fiap.rei_dos_piratas.infrastructure.config.usuario;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.FuncionarioServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioServiceConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository, ProdutoService produtoService) {
        return new ClienteServiceImpl(repository, produtoService);
    }

    @Bean
    public FuncionarioService vendedorService(FuncionarioRepository repository) {
        return new FuncionarioServiceImpl(repository);
    }

}
