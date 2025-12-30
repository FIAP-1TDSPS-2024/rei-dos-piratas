package br.com.fiap.rei_dos_piratas.interfaces.dto.frete;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FreteServiceDto(
        Long id,
        String name,
        double price,
        double customPrice,
        double discount,
        String currency,
        Integer deliveryTime,
        FreteCompanyDto company

) {}
