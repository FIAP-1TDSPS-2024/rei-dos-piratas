package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record DevolucaoFreteRequestDto(
        Integer service,
        String new_sender_mail,
        String new_sender_phone,
        BigDecimal insurance_value,
        UUID order_id,
        @JsonProperty("package") DevolucaoPackageDto packageDto,
        DevolucaoOptionsDto options) {
}