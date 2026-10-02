package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.infrastructure.security.SpringUsuarioDetailsService;
import br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class usuarioDetailsServiceConfig {

    @Bean
    public UsuarioDetailsService usuarioDetailsService(br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository contas) {
        return new SpringUsuarioDetailsService(contas);
    }
}
