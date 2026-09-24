package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DevolucaoFreteResponseDto(
        UUID id,
        String protocol,
        Integer service_id,
        BigDecimal quote,
        BigDecimal price,
        BigDecimal discount,
        Integer deliveryMin,
        Integer deliveryMax,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        Integer reverse
) {
}