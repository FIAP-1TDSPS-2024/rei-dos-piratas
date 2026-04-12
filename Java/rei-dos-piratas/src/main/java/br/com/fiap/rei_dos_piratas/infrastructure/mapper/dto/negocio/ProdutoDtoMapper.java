package br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;

public class ProdutoDtoMapper {

    public static Produto toEntity(ProdutoInDto produtoDto) {
        if (produtoDto == null) {
            return null;
        }
        return new Produto(
                produtoDto.id(),
                produtoDto.nome(),
                produtoDto.descricao(),
                produtoDto.autor(),
                produtoDto.categoria(),
                produtoDto.enderecoImagem(),
                produtoDto.preco(),
                produtoDto.precoOriginal(),
                produtoDto.estoque(),
                produtoDto.altura(),
                produtoDto.largura(),
                produtoDto.profundidade(),
                produtoDto.peso(),
                produtoDto.condicao(),
                null
                );
    }

    public static ProdutoOutDto toDto(Produto produto) {
        if (produto == null) {
            return null;
        }

        return new ProdutoOutDto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getAutor(),
                produto.getCategoria(),
                produto.getEnderecoImagem(),
                produto.getPreco(),
                produto.getPrecoOriginal(),
                produto.getEstoque(),
                produto.getAltura(),
                produto.getLargura(),
                produto.getProfundidade(),
                produto.getPeso(),
                produto.getCondicao()
        );
    }

    private ProdutoDtoMapper() {}
}
