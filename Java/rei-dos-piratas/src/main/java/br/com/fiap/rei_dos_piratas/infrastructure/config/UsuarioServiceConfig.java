package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.VendedorService;
import br.com.fiap.rei_dos_piratas.application.service.impl.ClienteServiceImpl;
import br.com.fiap.rei_dos_piratas.application.service.impl.VendedorServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.VendedorRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioServiceConfig {

    @Bean
    public ClienteService clienteService(ClienteRepository repository) {
        return new ClienteServiceImpl(repository);
    }

    @Bean
    public VendedorService vendedorService(VendedorRepository repository) {
        return new VendedorServiceImpl(repository);
    }

}
