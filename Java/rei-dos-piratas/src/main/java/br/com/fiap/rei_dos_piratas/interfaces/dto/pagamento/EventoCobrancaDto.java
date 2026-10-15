package br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoCobrancaDto (
    String id,
    String event,

    @JsonProperty("dateCreated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateCreated,

    Payment payment,

    @JsonProperty("additionalInfo")
    AdditionalInfo additionalInfo
)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payment(
            String id,
            String status,
            @JsonProperty("externalReference")
            String externalReference
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdditionalInfo(
            @JsonProperty("denialReason")
            String denialReason
    ) {}
}
