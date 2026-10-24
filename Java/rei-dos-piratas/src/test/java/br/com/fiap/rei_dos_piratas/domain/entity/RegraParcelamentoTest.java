package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegraParcelamentoTest {

    // Regra padrão do negócio: a partir de R$ 100,00, em até 3 vezes
    private final RegraParcelamento regra = new RegraParcelamento(new BigDecimal("100.00"), 3);

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Permite parcelar até o máximo quando o total atinge o valor mínimo")
    void devePermitirParcelarAteOMaximoQuandoTotalAtingeOMinimo(int parcelas) {
        assertThatCode(() -> regra.validar(new BigDecimal("100.00"), parcelas))
                .doesNotThrowAnyException();
    }

    @Test
    void deveRecusarParcelasAcimaDoMaximo() {
        assertThatThrownBy(() -> regra.validar(new BigDecimal("500.00"), 4))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("3");
    }

    @Test
    void deveRecusarParcelamentoQuandoTotalAbaixoDoMinimo() {
        assertThatThrownBy(() -> regra.validar(new BigDecimal("99.99"), 2))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("100");
    }

    @Test
    void devePermitirPagamentoAVistaQuandoTotalAbaixoDoMinimo() {
        assertThatCode(() -> regra.validar(new BigDecimal("10.00"), 1))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void deveRecusarQuantidadeDeParcelasMenorQueUm(int parcelas) {
        assertThatThrownBy(() -> regra.validar(new BigDecimal("200.00"), parcelas))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
