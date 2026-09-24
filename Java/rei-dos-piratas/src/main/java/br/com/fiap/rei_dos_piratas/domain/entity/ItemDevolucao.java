package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDevolucao {

    private Long id;

    private ItemProdutoPedido itemPedido;

    private int quantidade;
}

