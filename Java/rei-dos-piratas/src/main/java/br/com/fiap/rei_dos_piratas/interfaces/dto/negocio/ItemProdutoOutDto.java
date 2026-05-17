package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemProdutoOutDto(
        @NotNull(message = "O produto não pode ser nulo")
        ProdutoOutDto produto,
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        int quantidade
) {
    /** Subtotal calculado: preço unitário × quantidade. */
    public BigDecimal subtotal() {
        if (produto == null || produto.preco() == null) return BigDecimal.ZERO;
        return produto.preco().multiply(BigDecimal.valueOf(quantidade));
    }
}
