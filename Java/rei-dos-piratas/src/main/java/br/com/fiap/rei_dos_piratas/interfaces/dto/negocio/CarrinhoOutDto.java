package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import java.util.List;

public record CarrinhoOutDto(
        Long id,
        List<ItemProdutoOutDto> produtosAdicionados
){}
