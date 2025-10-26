package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemProduto {
    private Long id;

    @NotNull(message = "O produto não pode ser nulo")
    private Produto produto;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private int quantidade;

    public ItemProduto(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }
}
