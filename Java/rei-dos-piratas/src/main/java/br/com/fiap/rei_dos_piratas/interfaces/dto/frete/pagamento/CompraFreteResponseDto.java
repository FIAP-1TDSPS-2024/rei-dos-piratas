package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CompraFreteResponseDto(
        String digitable,
        String redirect,
        String message,
        String token,
        String payment_id,
        DetalhesPagamentoFreteDto purchase) {

    private record DetalhesPagamentoFreteDto(
            String id,
            String protocol,
            BigDecimal total,
            BigDecimal discount,
            String status,
            @JsonProperty("paid_at")
            String paidAt,
            @JsonProperty("canceled_at")
            String canceledAt,
            @JsonProperty("created_at")
            String createdAt,
            @JsonProperty("updated_at")
            String updatedAt,
            Object payment
    ) {}
}


