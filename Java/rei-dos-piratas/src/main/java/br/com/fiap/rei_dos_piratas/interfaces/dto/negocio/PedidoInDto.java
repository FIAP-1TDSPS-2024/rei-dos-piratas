package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoInDto(
        @NotNull
        Long freteServiceId,
        @NotNull
        Long EnderecoEntregaId,
        @NotNull
        List<ItemProdutoInDto> produtosAdicionados,
        @NotNull
        TipoPagamentoEnum tipoPagamento,
        @Valid
        CartaoPedidoInDto cartao
) {
    public PedidoInDto(Long freteServiceId, Long EnderecoEntregaId, List<ItemProdutoInDto> produtosAdicionados, TipoPagamentoEnum tipoPagamento) {
        this(freteServiceId, EnderecoEntregaId, produtosAdicionados, tipoPagamento, null);
    }
}
