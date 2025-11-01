package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;

public record PedidoOutDto(
        Long id,

        @PastOrPresent(message = "A data de criação do pedido deve estar no presente ou passado")
        @NotNull(message = "A data de criação do pedido não pode ser nula")
        LocalDate dataPedido,

        @PastOrPresent(message = "A data de entrega do pedido deve estar no presente ou passado")
        LocalDate dataEntrega,

        @PastOrPresent(message = "A data de cancelamento do pedido deve estar no presente ou passado")
        LocalDate dataCancelamento,

        @Digits(fraction = 2, integer = 6, message = "O preço total do produto deve ter até 8 digitos com 2 dígitos após a vírgula")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preço total não pode ser negativo")
        float valorTotal,

        @NotNull(message = "O status do pedido não pode ser nulo")
        StatusEnum status,

        @NotNull(message = "Os produtos adicionados do pedido não pode ser nulo")
        List<ItemProdutoOutDto> produtosAdicionados
) {
}
