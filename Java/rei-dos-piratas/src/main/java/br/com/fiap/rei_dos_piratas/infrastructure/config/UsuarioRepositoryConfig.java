package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.VendedorRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.VendedorRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaVendedorEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioRepositoryConfig {

    @Bean
    public ClienteRepository clienteRepository(JpaClienteEntityRepository jpaClienteEntityRepository) {
        return new ClienteRepositoryImpl(jpaClienteEntityRepository);
    }

    @Bean
    public VendedorRepository vendedorRepository(JpaVendedorEntityRepository jpaVendedorEntityRepository) {
        return new VendedorRepositoryImpl(jpaVendedorEntityRepository);
    }
}
