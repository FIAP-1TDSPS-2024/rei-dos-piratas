package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record TokenizacaoCartaoRequestDto(
        String customer,
        CartaoCreditoRequestDto creditCard,
        TitularCartaoRequestDto creditCardHolderInfo
) {}
