package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido;

import java.util.List;

public record PedidoFreteRequestDto(
        Long service,
        DestinoRemetenteDto from,
        DestinoRemetenteDto to,
        List<ProdutoPedidoFreteDto> products,
        List<VolumeFreteDto> volumes,
        FreteOptionsDto options
) {}
