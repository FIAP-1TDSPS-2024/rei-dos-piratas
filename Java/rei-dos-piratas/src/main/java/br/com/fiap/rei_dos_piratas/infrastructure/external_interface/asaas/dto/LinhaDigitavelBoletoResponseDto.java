package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Resposta de GET /v3/payments/{id}/identificationField do Asaas. */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record LinhaDigitavelBoletoResponseDto(
        String identificationField,
        String nossoNumero,
        String barCode
) {}
