package br.com.fiap.rei_dos_piratas.infrastructure.config.transacao;

import br.com.fiap.rei_dos_piratas.domain.gateway.ExecutorTransacional;
import br.com.fiap.rei_dos_piratas.infrastructure.transacao.ExecutorTransacionalSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransacaoConfig {

    @Bean
    public ExecutorTransacional executorTransacional(PlatformTransactionManager transactionManager) {
        return new ExecutorTransacionalSpring(new TransactionTemplate(transactionManager));
    }
}
