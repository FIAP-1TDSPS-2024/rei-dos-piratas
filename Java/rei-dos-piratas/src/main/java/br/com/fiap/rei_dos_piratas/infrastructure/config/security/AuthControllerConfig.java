package br.com.fiap.rei_dos_piratas.infrastructure.config.security;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.AuthControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
public class AuthControllerConfig {

    @Bean
    public AuthController authController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, ClienteService clienteService) {
        return new AuthControllerImpl(authenticationManager, jwtUtil, clienteService);
    }
}
