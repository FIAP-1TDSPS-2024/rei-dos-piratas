package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record TokenizacaoCartaoRequestDto(
        String customer,
        CartaoCreditoRequestDto creditCard,
        TitularCartaoRequestDto creditCardHolderInfo,
        String remoteIp
) {}
