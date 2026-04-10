package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Length(min = 10, max = 150, message = "O nome do produto deve ter entre 10 e 150 caracteres")
    private String nome;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Length(min = 10, max = 500, message = "A descrição do produto deve ter entre 10 e 500 caracteres")
    private String descricao;

    @NotBlank(message = "O autor do produto é obrigatório")
    @Length(min = 3, max = 100, message = "O nome do autor deve ter entre 3 e 100 caracteres")
    private String autor;

    @NotNull(message = "A categoria do produto é obrigatória")
    private CategoriaEnum categoria;

    @Length(max = 255, message = "A URL da imagem deve ter até 255 caracteres")
    private String enderecoImagem;

    @NotNull(message = "O preço do produto é obrigatório")
    @Digits(fraction = 2, integer = 6, message = "O preço deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço não pode ser zero ou negativo")
    private BigDecimal preco;

    @Digits(fraction = 2, integer = 6, message = "O preço original deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço original não pode ser zero ou negativo")
    private BigDecimal precoOriginal;

    @Min(value = 0, message = "O estoque não pode ser negativo")
    private int estoque;

    @Digits(fraction = 2, integer = 6, message = "A altura deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "A altura não pode ser zero ou negativa")
    private BigDecimal altura;

    @Digits(fraction = 2, integer = 6, message = "A largura deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "A largura não pode ser zero ou negativa")
    private BigDecimal largura;

    @Digits(fraction = 2, integer = 6, message = "A profundidade deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "A profundidade não pode ser zero ou negativa")
    private BigDecimal profundidade;

    @Digits(fraction = 2, integer = 6, message = "O peso deve ter até 8 dígitos com 2 após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O peso não pode ser zero ou negativo")
    private BigDecimal peso;

    @NotNull(message = "A condição do produto é obrigatória (NOVO ou USADO)")
    private CondicaoEnum condicao;

    private Funcionario funcionario;

    public Produto(String nome, String descricao, String autor, CategoriaEnum categoria, String enderecoImagem, BigDecimal preco, BigDecimal precoOriginal, int estoque, BigDecimal altura, BigDecimal largura, BigDecimal profundidade, BigDecimal peso, CondicaoEnum condicao, Funcionario funcionario) {
        this.nome = nome;
        this.descricao = descricao;
        this.autor = autor;
        this.categoria = categoria;
        this.enderecoImagem = enderecoImagem;
        this.preco = preco;
        this.precoOriginal = precoOriginal;
        this.estoque = estoque;
        this.altura = altura;
        this.largura = largura;
        this.peso = peso;
        this.profundidade = profundidade;
        this.condicao = condicao;
        this.funcionario = funcionario;
    }

    public Produto(Long id) {
        this.id = id;
    }
}
