package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.CartaoClienteRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCartaoClienteEntityRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartaoClienteRepositoryConfig {

    @Bean
    public CartaoClienteRepository cartaoClienteRepository(JpaCartaoClienteEntityRepository jpaCartaoClienteEntityRepository, EntityManager entityManager) {
        return new CartaoClienteRepositoryImpl(jpaCartaoClienteEntityRepository, entityManager);
    }
}
