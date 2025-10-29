package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carrinho {
    private Long id;

    @NotNull(message = "O pedido deve ter um cliente associado")
    private Cliente cliente;

    @Size(min = 1, message = "O pedido deve ter pelo menos um produto")
    private List<ItemProduto> produtosAdicionados;
}
