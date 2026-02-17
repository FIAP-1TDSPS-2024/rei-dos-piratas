package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoFreteResponseDto(
        UUID id,
        String protocol,
        Integer serviceId,
        Integer agencyId,
        BigDecimal quote,
        BigDecimal price,
        BigDecimal discount,
        Integer deliveryMin,
        Integer deliveryMax,
        String status,
        Boolean risk,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}

