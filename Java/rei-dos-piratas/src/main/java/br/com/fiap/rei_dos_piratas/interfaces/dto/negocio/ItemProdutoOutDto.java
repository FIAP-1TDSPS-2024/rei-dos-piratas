package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemProdutoOutDto(
        @NotNull(message = "O produto não pode ser nulo")
        ProdutoOutDto produto,
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        int quantidade, BigDecimal precoUnitario
) {
    public ItemProdutoOutDto(ProdutoOutDto produto, int quantidade) {
        this(produto, quantidade, produto.preco());
    }
    /** Subtotal calculado: preço unitário × quantidade. */
    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
