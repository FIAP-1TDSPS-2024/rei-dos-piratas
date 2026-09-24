package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

public record ItemDevolucaoOutDto(
        Long id,
        Long itemPedidoId,
        Long produtoId,
        String produtoNome,
        Integer quantidade
) {
}

