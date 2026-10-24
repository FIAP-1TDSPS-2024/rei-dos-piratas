package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

/**
 * Regra de parcelamento no cartão. Valores vêm de configuração
 * (PAGAMENTO_PARCELAMENTO_VALOR_MINIMO e PAGAMENTO_PARCELAMENTO_MAX_PARCELAS).
 */


public record RegraParcelamento(

        @NotNull(message = "O valor mínimo para parcelamento é obrigatório")
        @DecimalMin(value = "0.0", message = "O valor mínimo para parcelamento não pode ser negativo")
        BigDecimal valorMinimo,
        @NotNull(message = "O máximo de parcelas é obrigatório")
        @Min(value = 1, message = "O máximo de parcelas deve ser pelo menos 1")
        int maxParcelas
) {

    public void validar(BigDecimal valorTotal, int parcelas) {

        if (parcelas < 1) {
            throw new RegraDeNegocioException("O número de parcelas deve ser pelo menos 1");
        }

        if (parcelas > maxParcelas) {
            throw new RegraDeNegocioException(
                    String.format("O máximo de parcelas permitido é %d", maxParcelas));
        }
        if (valorTotal.compareTo(valorMinimo) < 0 && parcelas > 1) {
            throw new RegraDeNegocioException(
                    String.format("O valor mínimo para parcelamento é %s", valorMinimo));
        }
    }
}
