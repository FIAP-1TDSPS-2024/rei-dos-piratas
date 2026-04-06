package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;

import java.math.BigDecimal;

/** DTO de saída — apenas transporta dados, sem anotações de validação. */
public record ProdutoOutDto(
        Long id,
        String nome,
        String descricao,
        String autor,
        CategoriaEnum categoria,
        String enderecoImagem,
        BigDecimal preco,
        BigDecimal precoOriginal,
        int estoque,
        BigDecimal altura,
        BigDecimal largura,
        BigDecimal profundidade,
        BigDecimal peso,
        CondicaoEnum condicao
) {}
