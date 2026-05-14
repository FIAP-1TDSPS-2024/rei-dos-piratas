package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record RastreioDataDto(
        String id,

        String protocol,

        String status,

        String tracking,

        @JsonProperty("self_tracking")
        String selfTracking,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("paid_at")
        OffsetDateTime paidAt,

        @JsonProperty("generated_at")
        OffsetDateTime generatedAt,

        @JsonProperty("posted_at")
        OffsetDateTime postedAt,

        @JsonProperty("delivered_at")
        OffsetDateTime deliveredAt,

        @JsonProperty("canceled_at")
        OffsetDateTime canceledAt,

        @JsonProperty("expired_at")
        OffsetDateTime expiredAt,

        @JsonProperty("tracking_url")
        String trackingUrl
) {
}
