package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.domain.repository.MotivoDevolucaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.MotivoDevolucaoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaMotivoDevolucaoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MotivoDevolucaoRepositoryConfig {

    @Bean
    public MotivoDevolucaoRepository motivoDevolucaoRepository(JpaMotivoDevolucaoEntityRepository jpaMotivoDevolucaoEntityRepository) {
        return new MotivoDevolucaoRepositoryImpl(jpaMotivoDevolucaoEntityRepository);
    }
}

