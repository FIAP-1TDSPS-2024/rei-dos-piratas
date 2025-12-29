package br.com.fiap.rei_dos_piratas.interfaces.dto.frete;

import java.util.List;
import java.util.Map;

public record ConsultaFreteServiceDto(
        Map<String, String> from,
        Map<String, String> to,
        List<ProdutoFreteDto> products
) {}
