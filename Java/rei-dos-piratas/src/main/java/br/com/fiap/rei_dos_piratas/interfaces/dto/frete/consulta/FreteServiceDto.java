package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FreteServiceDto(
        Long id,
        String name,
        BigDecimal price,
        BigDecimal customPrice,
        BigDecimal discount,
        String currency,
        Integer deliveryTime,
        FreteCompanyDto company

) {}
