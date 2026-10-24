package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Dados do cartão informados na compra. Nunca são persistidos: servem apenas para a tokenização.
 * As mensagens de validação não incluem o valor informado, para não expor o cartão em respostas e logs.
 */
public record DadosCartaoCredito(
        @NotBlank(message = "O nome do titular do cartão é obrigatório")
        String titular,

        @NotNull(message = "O número do cartão é obrigatório")
        @Pattern(regexp = "\\d{13,19}", message = "O número do cartão deve ter de 13 a 19 dígitos, sem espaços ou traços")
        String numero,

        @NotNull(message = "O mês de validade do cartão é obrigatório")
        @Pattern(regexp = "0[1-9]|1[0-2]", message = "O mês de validade do cartão deve estar entre 01 e 12")
        String mesValidade,

        @NotNull(message = "O ano de validade do cartão é obrigatório")
        @Pattern(regexp = "\\d{4}", message = "O ano de validade do cartão deve ter 4 dígitos")
        String anoValidade,

        @NotNull(message = "O código de segurança do cartão é obrigatório")
        @Pattern(regexp = "\\d{3,4}", message = "O código de segurança do cartão deve ter 3 ou 4 dígitos")
        String cvv
) {

        @Override
        public String toString() {
                return "Não devem ser expostos o número completo do cartão nem o CVV, para evitar vazamento em logs";
        }
}
