package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

/**
 * Regra de vencimento do boleto. {@code diasVencimento} vem de PAGAMENTO_BOLETO_DIAS_VENCIMENTO.
 */
public record RegraBoleto(
        @Min(value = 1, message = "O boleto deve vencer em pelo menos 1 dia")
        int diasVencimento
) {

    /** Dias após o vencimento para o Asaas cancelar o registro (boleto deixa de ser pagável). */
    public static final int DIAS_CANCELAMENTO_REGISTRO = 1;

    public LocalDate vencimentoAPartirDe(LocalDate hoje) {
        return hoje.plusDays(diasVencimento);
    }
}
