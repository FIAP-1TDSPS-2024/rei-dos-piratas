package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Pedido {
    private Long id;

    @PastOrPresent(message = "A data de criação do pedido deve estar no presente ou passado")
    @NotNull(message = "A data de criação do pedido não pode ser nula")
    private LocalDate dataPedido;

    @PastOrPresent(message = "A data de entrega do pedido deve estar no presente ou passado")
    private LocalDate dataEntrega;

    @PastOrPresent(message = "A data de cancelamento do pedido deve estar no presente ou passado")
    private LocalDate dataCancelamento;

    @Digits(fraction = 2, integer = 6, message = "O preço total do produto deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço total não pode ser negativo")
    private float valorTotal;

    @NotNull(message = "O status do pedido não pode ser nulo")
    private StatusEnum status;

    @NotNull(message = "O pedido deve ter um cliente associado")
    private Cliente cliente;

    @Size(min = 1, message = "O pedido deve ter pelo menos um produto")
    private List<ItemProduto> produtosAdicionados;
}
