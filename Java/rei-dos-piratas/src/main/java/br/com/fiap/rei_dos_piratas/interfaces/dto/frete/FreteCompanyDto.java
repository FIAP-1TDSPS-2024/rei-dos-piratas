package br.com.fiap.rei_dos_piratas.interfaces.dto.frete;

public record FreteCompanyDto(
        // Company (flattened)
        Long id,
        String name,
        String picture
) {}
