package br.com.fiap.rei_dos_piratas.infrastructure.config.pedido;

import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.DevolucaoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaDevolucaoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevolucaoRepositoryConfig {

    @Bean
    public DevolucaoRepository devolucaoRepository(JpaDevolucaoEntityRepository jpaDevolucaoEntityRepository) {
        return new DevolucaoRepositoryImpl(jpaDevolucaoEntityRepository);
    }
}

