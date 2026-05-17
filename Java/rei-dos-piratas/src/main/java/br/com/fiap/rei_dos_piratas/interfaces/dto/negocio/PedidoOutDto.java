package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoOutDto(
        Long id,

        @PastOrPresent(message = "A data de criação do pedido deve estar no presente ou passado")
        @NotNull(message = "A data de criação do pedido não pode ser nula")
        LocalDate dataPedido,

        @PastOrPresent(message = "A data de entrega do pedido deve estar no presente ou passado")
        LocalDate dataEntrega,

        LocalDate previsaoEntrega,

        @PastOrPresent(message = "A data de cancelamento do pedido deve estar no presente ou passado")
        LocalDate dataCancelamento,

        @Digits(fraction = 2, integer = 6, message = "O preço total do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preço total não pode ser negativo")
        BigDecimal valorTotal,

        @Digits(fraction = 2, integer = 6, message = "O valor do frete do pedido deve ter até 8 digitos com 2 dígitos após a vírgula")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor do frete não pode ser negativo")
        BigDecimal valorFrete,

        @NotNull(message = "O status do pedido não pode ser nulo")
        StatusEnum status,

        @NotNull(message = "Os produtos adicionados do pedido não pode ser nulo")
        List<ItemProdutoOutDto> produtosAdicionados,

        // Dados do cliente e entrega
        String nomeCliente,
        // Endereço de entrega formatado (ex: "Rua X, 10, Centro, 01310-100 - Brasil")
        String enderecoEntrega,

        // Dados de rastreio e envio (Melhor Envio)
        String notaFiscal,
        UUID pedidoFrete,
        String protocoloEnvio,
        // Status de entrega conforme retornado pelo webhook do Melhor Envio
        String statusEnvio,
        // Código de rastreio
        String tracking,
        // URL de rastreio
        String trackingUrl
) {
}
