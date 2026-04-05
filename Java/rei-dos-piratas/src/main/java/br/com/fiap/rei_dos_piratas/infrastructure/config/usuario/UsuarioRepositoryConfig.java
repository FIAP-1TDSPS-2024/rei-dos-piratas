package br.com.fiap.rei_dos_piratas.infrastructure.config.usuario;

import br.com.fiap.rei_dos_piratas.domain.repository.ClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.FuncionarioRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.RoleRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.ClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.FuncionarioRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.PerfilRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.RoleRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaClienteEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaFuncionarioEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPerfilEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaRoleEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioRepositoryConfig {

    @Bean
    public ClienteRepository clienteRepository(JpaClienteEntityRepository jpaClienteEntityRepository) {
        return new ClienteRepositoryImpl(jpaClienteEntityRepository);
    }

    @Bean
    public FuncionarioRepository funcionarioRepository(JpaFuncionarioEntityRepository jpaFuncionarioEntityRepository) {
        return new FuncionarioRepositoryImpl(jpaFuncionarioEntityRepository);
    }

    @Bean
    public RoleRepository roleRepository(JpaRoleEntityRepository jpaRoleEntityRepository) {
        return new RoleRepositoryImpl(jpaRoleEntityRepository);
    }

    @Bean
    public PerfilRepository perfilRepository(JpaPerfilEntityRepository jpaPerfilEntityRepository) {
        return new PerfilRepositoryImpl(jpaPerfilEntityRepository);
    }
}
