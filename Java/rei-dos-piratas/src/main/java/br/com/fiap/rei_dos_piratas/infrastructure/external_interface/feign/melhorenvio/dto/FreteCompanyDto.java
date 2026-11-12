package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.melhorenvio.dto;

public record FreteCompanyDto(
        // Company (flattened)
        Long id,
        String name,
        String picture
) {}
