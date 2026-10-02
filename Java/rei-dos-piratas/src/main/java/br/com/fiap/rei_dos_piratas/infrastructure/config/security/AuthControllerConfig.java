package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.AutenticacaoServiceImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.AuthControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthControllerConfig {
    @Bean
    public AutenticacaoService autenticacaoService(ContaRepository contas, SenhaService senhas,
                                                  TokenAcessoService tokens, ClienteService clientes) {
        return new AutenticacaoServiceImpl(contas, senhas, tokens, clientes);
    }

    @Bean
    public AuthController authController(AutenticacaoService autenticacao) {
        return new AuthControllerImpl(autenticacao);
    }
}
