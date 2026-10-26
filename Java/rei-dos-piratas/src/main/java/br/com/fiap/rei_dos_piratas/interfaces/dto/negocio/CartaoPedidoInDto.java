package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.DadosCartaoCredito;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cartão informado no pedido: {@code cartaoId} de um cartão salvo ou os dados de um cartão novo.
 */
public record CartaoPedidoInDto(
        @Positive(message = "O ID do cartão salvo deve ser maior que zero")
        Long cartaoId,
        String titular,
        String numero,
        String mesValidade,
        String anoValidade,
        String cvv,
        @NotNull(message = "O pagamento deve ter pelo menos uma parcela")
        @Min(value = 1, message = "O pagamento deve ter pelo menos uma parcela")
        Integer parcelas
) {
    @JsonIgnore
    @Valid
    public DadosCartaoCredito getDadosCartaoCredito() {
        if (cartaoId != null) {
            return null;
        }

        return new DadosCartaoCredito(titular, numero, mesValidade, anoValidade, cvv);
    }
}
