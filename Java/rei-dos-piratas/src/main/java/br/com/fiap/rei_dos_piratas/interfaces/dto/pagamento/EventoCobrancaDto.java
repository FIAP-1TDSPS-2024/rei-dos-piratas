package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoCobrancaDto (
    String id,
    String event,

    @JsonProperty("dateCreated")
    LocalDateTime dateCreated,

    Payment payment,

    AdditionalInfo additionalInfo
)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payment(
            String id,
            String status,
            String externalReference
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalInfo(
            String denialReason
    ) {}
}
