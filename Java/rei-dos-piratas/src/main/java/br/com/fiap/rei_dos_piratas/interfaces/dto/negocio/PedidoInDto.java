package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoInDto(
        @NotNull
        Long freteServiceId,
        @NotNull
        Long EnderecoEntregaId,
        @NotNull
        List<ItemProdutoInDto> produtosAdicionados
) {}
