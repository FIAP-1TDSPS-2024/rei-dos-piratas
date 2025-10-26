package br.com.fiap.rei_dos_piratas.infrastructure.config;

import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.FuncionarioRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioRepositoryConfig {

    @Bean
    public ClienteRepository clienteRepository(JpaClienteEntityRepository jpaClienteEntityRepository) {
        return new ClienteRepositoryImpl(jpaClienteEntityRepository);
    }

    @Bean
    public FuncionarioRepository vendedorRepository(JpaFuncionarioEntityRepository jpaFuncionarioEntityRepository) {
        return new FuncionarioRepositoryImpl(jpaFuncionarioEntityRepository);
    }
}
