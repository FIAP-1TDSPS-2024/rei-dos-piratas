package br.com.fiap.rei_dos_piratas.interfaces.dto;

import java.util.List;

public record CarrinhoOutDto(
        Long id,
        List<ItemProdutoOutDto> produtosAdicionados
){}
