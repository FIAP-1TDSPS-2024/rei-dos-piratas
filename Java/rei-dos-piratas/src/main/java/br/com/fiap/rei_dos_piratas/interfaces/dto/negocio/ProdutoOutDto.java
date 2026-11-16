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
        CondicaoEnum condicao,
        /** URL para exibir a imagem (pré-assinada e temporária); {@code enderecoImagem} guarda só a chave. */
        String urlImagem,
        /** false = produto desativado ("excluído"): fora das vendas, mas ainda nos pedidos antigos. */
        boolean ativo
) {
    /** Sem a URL de exibição (preenchida depois pelo controller). */
    public ProdutoOutDto(Long id, String nome, String descricao, String autor, CategoriaEnum categoria,
                         String enderecoImagem, BigDecimal preco, BigDecimal precoOriginal, int estoque,
                         BigDecimal altura, BigDecimal largura, BigDecimal profundidade, BigDecimal peso,
                         CondicaoEnum condicao) {
        this(id, nome, descricao, autor, categoria, enderecoImagem, preco, precoOriginal, estoque,
                altura, largura, profundidade, peso, condicao, null, true);
    }

    public ProdutoOutDto comUrlImagem(String url) {
        return new ProdutoOutDto(id, nome, descricao, autor, categoria, enderecoImagem, preco, precoOriginal, estoque,
                altura, largura, profundidade, peso, condicao, url, ativo);
    }

    public ProdutoOutDto comAtivo(boolean ativo) {
        return new ProdutoOutDto(id, nome, descricao, autor, categoria, enderecoImagem, preco, precoOriginal, estoque,
                altura, largura, profundidade, peso, condicao, urlImagem, ativo);
    }
}
