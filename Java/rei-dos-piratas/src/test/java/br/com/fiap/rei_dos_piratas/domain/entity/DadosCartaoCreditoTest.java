package br.com.fiap.rei_dos_piratas.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DadosCartaoCreditoTest {

    @Test
    @DisplayName("toString não expõe o número completo nem o CVV (evita vazamento em logs)")
    void toStringNaoDeveExporNumeroCompletoNemCvv() {
        // Arrange
        DadosCartaoCredito cartao = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

        // Act
        String texto = cartao.toString();

        // Assert
        assertThat(texto)
                .doesNotContain("4111111111111234")
                .doesNotContain("987");
    }
}
