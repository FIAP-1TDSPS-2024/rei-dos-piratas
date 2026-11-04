package br.com.fiap.rei_dos_piratas.infrastructure.transacao;

import br.com.fiap.rei_dos_piratas.domain.gateway.ExecutorTransacional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/** {@link ExecutorTransacional} com o {@link TransactionTemplate} do Spring. */
public class ExecutorTransacionalSpring implements ExecutorTransacional {

    private final TransactionTemplate transactionTemplate;

    public ExecutorTransacionalSpring(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T emTransacao(Supplier<T> operacao) {
        return transactionTemplate.execute(status -> operacao.get());
    }
}
