package br.com.fiap.rei_dos_piratas.infrastructure.config.usuario;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.ClienteControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.FuncionarioControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioControllerConfig {
    @Bean
    public ClienteController clienteController(ClienteService service) {
        return new ClienteControllerImpl(service);
    }

    @Bean
    public FuncionarioController funcionarioController(FuncionarioService service) {
        return new FuncionarioControllerImpl(service);
    }
}
