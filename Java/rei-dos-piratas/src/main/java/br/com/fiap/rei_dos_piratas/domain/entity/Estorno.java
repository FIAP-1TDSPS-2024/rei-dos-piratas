package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Devolução de valor de um pagamento (cancelamento do pedido ou devolução de itens). Filha de {@link Pagamento}. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Estorno {
    private Long id;

    @NotNull(message = "A origem do estorno é obrigatória")
    private OrigemEstornoEnum origem;

    /** Devolução que originou o estorno (só origem DEVOLUCAO). */
    private Long devolucaoId;

    @NotNull(message = "O valor do estorno é obrigatório")
    @Digits(integer = 10, fraction = 2, message = "O valor do estorno deve ter até 12 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor do estorno deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "O status do estorno é obrigatório")
    private StatusEstornoEnum status;

    /** Boleto: link do fornecedor onde o cliente informa os dados bancários. */
    @Size(max = 500, message = "A URL de solicitação do estorno deve ter até 500 caracteres")
    private String urlSolicitacaoBoleto;

    @Size(max = 255, message = "O motivo da falha do estorno deve ter até 255 caracteres")
    private String motivoFalha;

    @NotNull(message = "A data de solicitação do estorno é obrigatória")
    @PastOrPresent(message = "A data de solicitação do estorno deve estar no presente ou passado")
    private LocalDateTime dataSolicitacao;

    @PastOrPresent(message = "A data de conclusão do estorno deve estar no presente ou passado")
    private LocalDateTime dataConclusao;
}
