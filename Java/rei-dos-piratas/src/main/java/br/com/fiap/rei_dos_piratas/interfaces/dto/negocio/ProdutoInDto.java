package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;

import java.math.BigDecimal;

/** DTO de entrada — apenas transporta dados, sem anotações de validação. */
public record ProdutoInDto(
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
        CondicaoEnum condicao,
        Long funcionarioId
) {}
