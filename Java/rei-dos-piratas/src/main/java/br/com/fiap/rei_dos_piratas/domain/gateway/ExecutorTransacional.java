package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.util.function.Supplier;

/**
 * Executa um trecho dentro de uma transação de banco. Usado quando o caso de uso precisa
 * delimitar transações explicitamente (ex.: gravar o pedido, chamar o fornecedor fora da
 * transação e gravar o resultado em outra).
 */
public interface ExecutorTransacional {

    /** Executa e confirma a transação; exceção não verificada desfaz a transação e é propagada. */
    <T> T emTransacao(Supplier<T> operacao);

    default void emTransacaoSemRetorno(Runnable operacao) {
        emTransacao(() -> {
            operacao.run();
            return null;
        });
    }
}
