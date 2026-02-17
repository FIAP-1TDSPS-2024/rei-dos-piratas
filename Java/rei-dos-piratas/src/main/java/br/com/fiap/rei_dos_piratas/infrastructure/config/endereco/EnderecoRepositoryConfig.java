package br.com.fiap.rei_dos_piratas.infrastructure.config.endereco;

import br.com.fiap.rei_dos_piratas.domain.repository.CidadeRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EnderecoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.EstadoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.CidadeRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.EnderecoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.EstadoRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl.DadosEmpresaRepositoryImpl;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaCidadeEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaDadosEmpresaEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEnderecoEntityRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEstadoEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnderecoRepositoryConfig {
    @Bean
    public EnderecoRepository enderecoRepository(JpaEnderecoEntityRepository repository) {
        return new EnderecoRepositoryImpl(repository);
    }

    @Bean
    public CidadeRepository cidadeRepository(JpaCidadeEntityRepository repository) {
        return new CidadeRepositoryImpl(repository);
    }

    @Bean
    public EstadoRepository estadoRepository(JpaEstadoEntityRepository repository) {
        return new EstadoRepositoryImpl(repository);
    }

    @Bean
    public DadosEmpresaRepository dadosEmpresaRepository(JpaDadosEmpresaEntityRepository repository) {
        return new DadosEmpresaRepositoryImpl(repository);
    }
}
