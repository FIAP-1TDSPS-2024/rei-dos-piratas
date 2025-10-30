package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoOutDto;

public class ProdutoDtoMapper {

    public static Produto toEntity(ProdutoInDto produtoDto) {
        if (produtoDto == null) {
            return null;
        }
        return new Produto(
                produtoDto.nome(),
                produtoDto.descricao(),
                produtoDto.enderecoImagem(), 
                produtoDto.preco(),
                produtoDto.estoque(),
                produtoDto.altura(),
                produtoDto.largura(),
                produtoDto.profundidade(),
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
                produto.getEnderecoImagem(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getAltura(),
                produto.getLargura(),
                produto.getProfundidade(),
                produto.getCondicao()
        );
    }

    private ProdutoDtoMapper() {}
}
