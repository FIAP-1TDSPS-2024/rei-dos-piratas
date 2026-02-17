package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;

import java.math.BigDecimal;
import java.util.List;

public record VolumeFreteDto(
        Integer height,
        Integer width,
        Integer length,
        BigDecimal weight
) { }
