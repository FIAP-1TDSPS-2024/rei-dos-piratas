package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

import java.math.BigDecimal;

public record FreteOptionsDto(

        String platform,
        String reminder,

        BigDecimal insuranceValue,

        Boolean receipt,

        Boolean ownHand,

        Boolean reverse,
        Boolean non_commercial,
        InvoiceDto invoice

) {}
