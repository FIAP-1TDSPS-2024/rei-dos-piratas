package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

public record FreteCompanyDto(
        // Company (flattened)
        Long id,
        String name,
        String picture
) {}
