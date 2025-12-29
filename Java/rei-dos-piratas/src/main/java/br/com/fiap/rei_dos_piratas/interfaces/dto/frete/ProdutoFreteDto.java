package br.com.fiap.rei_dos_piratas.interfaces.dto.frete;

import java.math.BigDecimal;

public record ProdutoFreteDto(
        Long id,
        BigDecimal width, //cm
        BigDecimal height, //cm
        BigDecimal length, //cm
        BigDecimal weight, //kg
        BigDecimal insurance_value, //BRL
        int quantity //unidade de produto
) {
}
