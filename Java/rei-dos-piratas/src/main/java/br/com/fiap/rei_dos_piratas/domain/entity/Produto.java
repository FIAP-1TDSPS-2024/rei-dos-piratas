package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    Long id;

    @NotNull(message = "O nome do produto não pode ser nulo")
    @Length(min = 10, max = 150, message = "O nome do produto deve ter entre 10 e 150 caracteres")
    private String nome;

    @NotNull(message = "A descrição do produto não pode ser nula")
    @Length(min = 10, max = 500, message = "A descrição do produto deve ter entre 10 e 150 caracteres")
    private String descricao;

    @NotNull(message = "O produto deve ter uma imagem associada")
    private String enderecoImagem;

    @Digits(fraction = 2, integer = 6, message = "O preço do produto deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço não pode ser negativo")
    private float preco;

    @Digits(fraction = 0, integer = 6, message = "O estoque deve ter 6 dígitos sem dígitos após a vírgula")
    @DecimalMin(value = "0", inclusive = false, message = "O estoque não pode ser negativo")
    private int estoque;

    @Digits(fraction = 0, integer = 6, message = "A altura do produto deve ter até 8 digitos com dois após a vírgula")
    @DecimalMin(value = "0", inclusive = false, message = "A altura do produto não pode ser negativa")
    private float altura;

    @Digits(fraction = 0, integer = 6, message = "A largura do produto deve ter até 8 digitos com dois após a vírgula")
    @DecimalMin(value = "0", inclusive = false, message = "A largura do produto não pode ser negativa")
    private float largura;

    @Digits(fraction = 0, integer = 6, message = "A profundidade do produto deve ter até 8 digitos com dois após a vírgula")
    @DecimalMin(value = "0", inclusive = false, message = "A profundidade do produto não pode ser negativa")
    private float profundidade;

    private CondicaoEnum condicao;

    private Funcionario funcionario;

    public Produto(String nome, String descricao, float preco, int estoque, float altura, float largura, float profundidade, CondicaoEnum condicao, Funcionario funcionario) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.altura = altura;
        this.largura = largura;
        this.profundidade = profundidade;
        this.condicao = condicao;
        this.funcionario = funcionario;
    }
}
