package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.frete;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.ProdutoFreteDto;

import java.math.BigDecimal;

public class ProdutoFreteDtoMapper {

    public static ProdutoFreteDto toDto(Produto produto, int quantidade) {
        return new ProdutoFreteDto(
                produto.getId(),
                produto.getLargura(),
                produto.getAltura(),
                produto.getProfundidade(),
                produto.getPeso(),
                produto.getPreco(),
                quantidade
        );
    }
}
