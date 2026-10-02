package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ProdutoCalculoFreteDto;

public class ProdutoFreteDtoMapper {

    public static ProdutoCalculoFreteDto toDto(Produto produto, int quantidade) {
        return toDto(produto, quantidade, produto.getPreco());
    }

    public static ProdutoCalculoFreteDto toDto(Produto produto, int quantidade, java.math.BigDecimal precoUnitario) {
        return new ProdutoCalculoFreteDto(
                produto.getId(),
                produto.getLargura(),
                produto.getAltura(),
                produto.getProfundidade(),
                produto.getPeso(),
                precoUnitario,
                quantidade
        );
    }
}
