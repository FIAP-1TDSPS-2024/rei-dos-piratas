package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta de POST /v3/payments/{id}/refund: a cobrança com a lista de estornos.
 * Status como texto: o Asaas pode acrescentar valores e um enum desconhecido quebraria a leitura.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record EstornoCobrancaResponseDto(String id, String status, List<Estorno> refunds) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public record Estorno(String status, BigDecimal value, String dateCreated) {}
}
