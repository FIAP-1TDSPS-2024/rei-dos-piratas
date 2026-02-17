package br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta;

import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConsultaFreteDto(
        @NotNull(message = "O CEP de origem não pode ser nulo")
        String cepOrigem,
        @NotNull(message = "O CEP de destino não pode ser nulo")
        String cepDestino,
        @NotNull(message = "Deve ser incluído pelo menos um produto")
        List<ItemProdutoInDto> itens
) {}
