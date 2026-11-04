package br.com.fiap.rei_dos_piratas.infrastructure.config.pagamento;

import br.com.fiap.rei_dos_piratas.domain.entity.RegraBoleto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BoletoConfig {

    @Bean
    public RegraBoleto regraBoleto(@Value("${app.pagamento.boleto.dias-vencimento}") int diasVencimento) {
        return new RegraBoleto(diasVencimento);
    }
}
