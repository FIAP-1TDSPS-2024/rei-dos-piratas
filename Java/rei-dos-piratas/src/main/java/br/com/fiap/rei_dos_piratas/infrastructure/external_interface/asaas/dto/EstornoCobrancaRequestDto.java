package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

/** Corpo de POST /v3/payments/{id}/refund (PIX e cartão). */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record EstornoCobrancaRequestDto(BigDecimal value, String description) {}
