package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta;

import java.math.BigDecimal;

public record ProdutoCalculoFreteDto(
        Long id,
        BigDecimal width, //cm
        BigDecimal height, //cm
        BigDecimal length, //cm
        BigDecimal weight, //kg
        BigDecimal insurance_value, //BRL
        int quantity //unidade de produto
) {
}
