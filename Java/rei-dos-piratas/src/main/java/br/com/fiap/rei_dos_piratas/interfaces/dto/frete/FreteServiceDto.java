package br.com.fiap.rei_dos_piratas.interfaces.dto.frete;

public record FreteServiceDto(
        Long id,
        String name,
        String price,
        String customPrice,
        String discount,
        String currency,
        Integer deliveryTime,

        // Company (flattened)
        Long companyId,
        String companyName,
        String companyPicture
) {}
