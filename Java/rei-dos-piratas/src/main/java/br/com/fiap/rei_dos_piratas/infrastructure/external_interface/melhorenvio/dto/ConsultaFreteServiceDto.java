package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

import java.util.List;
import java.util.Map;

public record ConsultaFreteServiceDto(
        Map<String, String> from,
        Map<String, String> to,
        List<ProdutoCalculoFreteDto> products
) {}
