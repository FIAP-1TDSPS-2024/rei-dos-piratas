package br.com.fiap.rei_dos_piratas.infrastructure.config.usuario;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.FuncionarioServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;

@Configuration
public class UsuarioServiceConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository, SenhaService passwordEncoder, PerfilRepository perfilRepository, Validator validator, ContaRepository contas,     UsuarioAtualService usuarioAtual, CobrancaAppClient apiCobranca) {
            return new ClienteServiceImpl(repository, passwordEncoder, perfilRepository, validator, contas, usuarioAtual, apiCobranca);
    }

    @Bean
    public FuncionarioService funcionarioService(FuncionarioRepository repository, SenhaService passwordEncoder, PerfilRepository perfilRepository, Validator validator, ContaRepository contas) {
        return new FuncionarioServiceImpl(repository, passwordEncoder, perfilRepository, validator, contas);
    }

}
