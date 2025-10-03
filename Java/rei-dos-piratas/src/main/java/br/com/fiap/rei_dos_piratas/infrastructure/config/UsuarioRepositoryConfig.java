package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioRepositoryConfig {

    @Bean
    public ClienteRepository clienteRepository(JpaClienteEntityRepository jpaClienteEntityRepository) {
        return new ClienteRepository(jpaClienteEntityRepository);
    }

    @Bean
    public VendedorRepository vendedorRepository(JpaVendedorEntityRepository jpaVendedorEntityRepository) {
        return new VendedorRepository(jpaVendedorEntityRepository);
    }
}
