package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.VendedorService;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.VendedorController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.ClienteControllerImpl;
import br.com.fiap.rei_dos_piratas.interfaces.controller.impl.VendedorControllerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioControllerConfig {
    @Bean
    public ClienteController clienteController(ClienteService service) {
        return new ClienteControllerImpl(service);
    }

    @Bean
    public VendedorController vendedorController(VendedorService service) {
        return new VendedorControllerImpl(service);
    }
}
