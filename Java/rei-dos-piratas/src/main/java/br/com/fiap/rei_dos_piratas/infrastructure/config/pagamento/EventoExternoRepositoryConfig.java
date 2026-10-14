package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.domain.repository.EventoExternoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.EventoExternoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEventoExternoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventoExternoRepositoryConfig {

    @Bean
    public EventoExternoRepository eventoExternoRepository(JpaEventoExternoEntityRepository jpaEventoExternoEntityRepository) {
        return new EventoExternoRepositoryImpl(jpaEventoExternoEntityRepository);
    }
}
