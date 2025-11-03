package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.application.service.impl.UsuarioDetailsServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class usuarioDetailsServiceConfig {

    @Bean
    public UsuarioDetailsService usuarioDetailsService(ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository) {
        return new UsuarioDetailsServiceImpl(clienteRepository, funcionarioRepository);
    }
}
