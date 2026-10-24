package br.com.fiap.rei_dos_piratas.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CartaoClienteTest {

    @Test
    @DisplayName("toString não expõe o token nem os últimos dígitos (evita vazamento em logs)")
    void toStringNaoDeveExporTokenNemUltimosDigitos() {
        // Arrange
        CartaoCliente cartao = new CartaoCliente(1L, null, "tok_secreto_abc", "9876", "VISA", true, LocalDateTime.now());

        // Act
        String texto = cartao.toString();

        // Assert
        assertThat(texto)
                .doesNotContain("tok_secreto_abc")
                .doesNotContain("9876");
    }
}
