package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carrinho {
    private Long id;

    @Size(min = 1, message = "O pedido deve ter pelo menos um produto")
    private List<ItemProdutoCarrinho> produtosAdicionados;
}
