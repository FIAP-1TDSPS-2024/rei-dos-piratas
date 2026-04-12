package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Formulário mutável para binding Thymeleaf.
 * Não possui anotações de validação — a validação é feita pela entidade
 * de domínio Produto dentro do ProdutoServiceImpl via jakarta.validation.Validator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoWebForm {

    private Long id;
    private String nome;
    private String descricao;
    private String autor;
    private CategoriaEnum categoria;
    private String enderecoImagem;
    private BigDecimal preco;
    private BigDecimal precoOriginal;
    private int estoque;
    private BigDecimal altura;
    private BigDecimal largura;
    private BigDecimal profundidade;
    private BigDecimal peso;
    private CondicaoEnum condicao;
    private Long funcionarioId;

    public static ProdutoWebForm fromOutDto(ProdutoOutDto dto) {
        if (dto == null) return new ProdutoWebForm();
        ProdutoWebForm form = new ProdutoWebForm();
        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setDescricao(dto.descricao());
        form.setAutor(dto.autor());
        form.setCategoria(dto.categoria());
        form.setEnderecoImagem(dto.enderecoImagem());
        form.setPreco(dto.preco());
        form.setPrecoOriginal(dto.precoOriginal());
        form.setEstoque(dto.estoque());
        form.setAltura(dto.altura());
        form.setLargura(dto.largura());
        form.setProfundidade(dto.profundidade());
        form.setPeso(dto.peso());
        form.setCondicao(dto.condicao());
        return form;
    }

    public ProdutoInDto toInDto() {
        return new ProdutoInDto(
                id, nome, descricao, autor, categoria, enderecoImagem,
                preco, precoOriginal, estoque, altura, largura,
                profundidade, peso, condicao, funcionarioId
        );
    }
}
