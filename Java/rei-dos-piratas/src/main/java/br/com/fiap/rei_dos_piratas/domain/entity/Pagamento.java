package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pagamento de um pedido (um por pedido; fica dentro do agregado Pedido). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pagamento {
    private Long id;

    @NotNull(message = "O tipo de pagamento é obrigatório")
    private TipoPagamentoEnum tipo;

    @NotNull(message = "O status do pagamento é obrigatório")
    private StatusPagamentoEnum status;

    /** ID da cobrança na API de pagamentos (Asaas). */
    @Size(max = 50, message = "O ID da cobrança deve ter até 50 caracteres")
    private String idCobranca;

    @NotNull(message = "O valor do pagamento é obrigatório")
    @Digits(integer = 10, fraction = 2, message = "O valor do pagamento deve ter até 12 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O valor do pagamento deve ser maior que zero")
    private BigDecimal valor;

    @Digits(integer = 10, fraction = 2, message = "O valor líquido deve ter até 12 dígitos com 2 após a vírgula")
    private BigDecimal valorLiquido;

    @NotNull(message = "A quantidade de parcelas é obrigatória")
    @Min(value = 1, message = "O pagamento deve ter pelo menos uma parcela")
    private Integer parcelas;

    @Valid
    private CartaoCliente cartao;

    @Size(max = 255, message = "O motivo da recusa deve ter até 255 caracteres")
    private String motivoRecusa;

    @NotNull(message = "A data de criação do pagamento é obrigatória")
    @PastOrPresent(message = "A data de criação do pagamento deve estar no presente ou passado")
    private LocalDateTime dataCriacao;

    @PastOrPresent(message = "A data de confirmação do pagamento deve estar no presente ou passado")
    private LocalDateTime dataConfirmacao;

    /** Pagamento escolhido na criação do pedido, antes de existir cobrança no Asaas. */
    public static Pagamento pendente(TipoPagamentoEnum tipo) {
        Pagamento pagamento = new Pagamento();
        pagamento.setTipo(tipo);
        pagamento.setStatus(StatusPagamentoEnum.PENDENTE);
        pagamento.setParcelas(1);
        pagamento.setDataCriacao(LocalDateTime.now());
        return pagamento;
    }
}
