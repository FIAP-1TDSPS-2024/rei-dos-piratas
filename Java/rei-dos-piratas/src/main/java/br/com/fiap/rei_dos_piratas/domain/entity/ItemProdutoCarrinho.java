package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ItemProdutoCarrinho {
    private Long id;

    @NotNull(message = "O produto não pode ser nulo")
    private Produto produto;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private int quantidade;

    @NotNull(message = "O preco unitario e obrigatorio")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "O preco unitario nao pode ser negativo")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    private BigDecimal precoUnitario;

    public ItemProdutoCarrinho(Long id, Produto produto, int quantidade) {
        this(id, produto, quantidade, produto.getPreco());
    }

    public ItemProdutoCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }
}
