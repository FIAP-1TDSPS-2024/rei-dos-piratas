package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.dto;

import java.util.List;

public record PedidoFreteRequestDto(
        Long service,
        DestinoRemetenteDto from,
        DestinoRemetenteDto to,
        List<ProdutoPedidoFreteDto> products,
        List<VolumeFreteDto> volumes,
        FreteOptionsDto options
) {}
