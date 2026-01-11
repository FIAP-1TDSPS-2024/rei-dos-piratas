package br.com.fiap.rei_dos_piratas.infrastructure.config.endereco;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.impl.EnderecoServiceImpl;
import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnderecoServiceConfig {
    @Bean
    public EnderecoService enderecoService(EnderecoRepository repository, ClienteService clienteService, CidadeRepository cidadeRepository, EstadoRepository estadoRepository) {
        return new EnderecoServiceImpl(repository, clienteService,  cidadeRepository, estadoRepository);
    }
}
