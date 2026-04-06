package br.com.fiap.rei_dos_piratas.infrastructure.config.usuario;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.FuncionarioServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UsuarioServiceConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository, Validator validator) {
        return new ClienteServiceImpl(repository, passwordEncoder, perfilRepository, validator);
    }

    @Bean
    public FuncionarioService funcionarioService(FuncionarioRepository repository, PasswordEncoder passwordEncoder, PerfilRepository perfilRepository, Validator validator) {
        return new FuncionarioServiceImpl(repository, passwordEncoder, perfilRepository, validator);
    }

}
