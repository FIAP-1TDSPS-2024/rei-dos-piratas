package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Pagamento com cartão solicitado junto do pedido: um cartão salvo ({@code idCartaoSalvo})
 * ou um cartão novo ({@code novoCartao}), nunca os dois.
 */
public record PagamentoCartao(

        Long idCartaoSalvo,

        @Valid
        DadosCartaoCredito novoCartao,

        @Min(value = 1, message = "O pagamento deve ter pelo menos uma parcela")
        int parcelas

        //@NotBlank(message = "O IP de quem faz a compra é obrigatório para pagamento com cartão")
        //String ipRemoto
) {
    public PagamentoCartao {
        if ((idCartaoSalvo == null) == (novoCartao == null)) {
            throw new RegraDeNegocioException("Informe um cartão salvo ou um cartão novo, e nunca os dois");
        }
    }
}
