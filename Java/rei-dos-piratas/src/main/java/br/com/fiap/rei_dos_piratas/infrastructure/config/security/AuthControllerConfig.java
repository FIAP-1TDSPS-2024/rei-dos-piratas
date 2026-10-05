package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.*;
import br.com.fiap.rei_dos_piratas.application.service.impl.AutenticacaoServiceImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.AuthControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import br.com.fiap.rei_dos_piratas.domain.repository.SessaoRepository;
import java.time.Clock;

@Configuration
public class AuthControllerConfig {
    @Bean
    public Clock authClock() { return Clock.systemUTC(); }

    @Bean
    public AutenticacaoService autenticacaoService(ContaRepository contas, SenhaService senhas,
                                                  TokenAcessoService tokens, ClienteService clientes,
                                                  SessaoRepository sessoes, RefreshTokenService refreshTokens, Clock clock) {
        return new AutenticacaoServiceImpl(contas, senhas, tokens, clientes, sessoes, refreshTokens, clock);
    }

    @Bean
    public AuthController authController(AutenticacaoService autenticacao, UsuarioAtualService usuarioAtual) {
        return new AuthControllerImpl(autenticacao, usuarioAtual);
    }
}
