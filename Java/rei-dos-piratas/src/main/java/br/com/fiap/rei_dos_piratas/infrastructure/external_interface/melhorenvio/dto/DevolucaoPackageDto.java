package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

import java.math.BigDecimal;

public record DevolucaoPackageDto(
        Integer height,
        Integer width,
        Integer length,
        BigDecimal weight) {
}
