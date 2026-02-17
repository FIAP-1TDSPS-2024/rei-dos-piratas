package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Digits(fraction = 2, integer = 6, message = "O preço total do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço total não pode ser negativo")
    private BigDecimal valorTotal;

    @Digits(fraction = 2, integer = 6, message = "O preço do frete do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço do frete do pedido não pode ser negativo")
    private BigDecimal valorFrete;

    @NotNull(message = "O status do pedido não pode ser nulo")
    private StatusEnum status;

    @NotNull(message = "O pedido deve ter um cliente associado")
    private Cliente cliente;

    @Size(min = 1, message = "O pedido deve ter pelo menos um produto")
    private List<ItemProdutoPedido> produtosAdicionados;

    @NotNull(message = "O pedido deve possuir um endereço para entrega")
    private Endereco enderecoEntrega;

    @NotNull(message = "O pedido deve definir um serviço para entrega")
    private Long servicoEntrega;

    private String notaFiscal;

    public Pedido(Cliente cliente, Endereco enderecoEntrega, List<ItemProdutoPedido> produtosAdicionados, Long servicoEntrega, BigDecimal valorFrete) {
        this.dataPedido = LocalDate.now();
        this.status = StatusEnum.AGUARDANDO_PAGAMENTO;
        this.valorTotal = produtosAdicionados
                .stream()
                .map(item ->
                        item.getProduto().getPreco()
                                .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorFrete = valorFrete;
        this.cliente = cliente;
        this.enderecoEntrega = enderecoEntrega;
        this.produtosAdicionados = produtosAdicionados;
        this.servicoEntrega = servicoEntrega;
    }
}