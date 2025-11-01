package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private List<ItemProdutoPedido> produtosAdicionados;

    public Pedido(Cliente cliente, List<ItemProdutoPedido> produtosAdicionados) {
        this.dataPedido = LocalDate.now();
        this.status = StatusEnum.AGUARDANDO_PAGAMENTO;
        this.valorTotal = (float) produtosAdicionados
                .stream()
                .mapToDouble(item -> item.getQuantidade() * item.getProduto().getPreco())
                .sum();
        this.cliente = cliente;
        this.produtosAdicionados = produtosAdicionados;
    }
}
