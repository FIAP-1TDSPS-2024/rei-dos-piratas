package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.impl.VendedorService;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioServiceConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository) {
        return new ClienteService(repository);
    }

    @Bean
    public VendedorService vendedorService(VendedorRepository repository) {
        return new VendedorService(repository);
    }

}
