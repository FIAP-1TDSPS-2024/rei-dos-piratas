package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDevolucao {

    private Long id;

    private ItemProdutoPedido itemPedido;

    private int quantidade;
    @jakarta.validation.constraints.NotNull(message = "O preco unitario e obrigatorio")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "O preco unitario nao pode ser negativo")
    @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
    private BigDecimal precoUnitario;
    public ItemDevolucao(Long id, ItemProdutoPedido itemPedido, int quantidade) {
        this(id, itemPedido, quantidade, itemPedido.getPrecoUnitario());
    }
}

