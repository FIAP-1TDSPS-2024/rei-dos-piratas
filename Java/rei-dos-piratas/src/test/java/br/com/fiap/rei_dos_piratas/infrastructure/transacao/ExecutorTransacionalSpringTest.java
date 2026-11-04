package br.com.fiap.rei_dos_piratas.infrastructure.transacao;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExecutorTransacionalSpringTest {

    private PlatformTransactionManager transactionManager;
    private ExecutorTransacionalSpring executor;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus status = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any())).thenReturn(status);
        executor = new ExecutorTransacionalSpring(new TransactionTemplate(transactionManager));
    }

    @Test
    void deveConfirmarATransacaoERetornarOResultado() {
        // Act
        String resultado = executor.emTransacao(() -> "ok");

        // Assert
        assertThat(resultado).isEqualTo("ok");
        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void excecaoDeveDesfazerATransacaoESerPropagada() {
        // Act & Assert
        assertThatThrownBy(() -> executor.emTransacao(() -> {
            throw new RegraDeNegocioException("falhou");
        })).isInstanceOf(RegraDeNegocioException.class).hasMessage("falhou");
        verify(transactionManager).rollback(any());
        verify(transactionManager, never()).commit(any());
    }

    @Test
    void semRetornoDeveExecutarDentroDaTransacao() {
        // Arrange
        StringBuilder executou = new StringBuilder();

        // Act
        executor.emTransacaoSemRetorno(() -> executou.append("sim"));

        // Assert
        assertThat(executou).hasToString("sim");
        verify(transactionManager).commit(any());
    }
}
