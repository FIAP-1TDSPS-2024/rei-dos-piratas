package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.validation.constraints.NotNull;

public record PedidoCarrinhoInDto(
        @NotNull
        Long freteServiceId,
        @NotNull
        Long EnderecoEntregaId,
        @NotNull
        TipoPagamentoEnum tipoPagamento) {}
