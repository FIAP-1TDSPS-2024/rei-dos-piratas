package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao;

import java.math.BigDecimal;

public record DevolucaoPackageDto(
        Integer height,
        Integer width,
        Integer length,
        BigDecimal weight) {
}
