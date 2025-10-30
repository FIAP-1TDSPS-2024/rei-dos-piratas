package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record ProdutoOutDto(

        @NotNull(message = "O ID não deve estar nulo para exibição ao usuário")
        Long id,

        @NotNull(message = "O nome do produto não pode ser nulo")
        @Length(min = 10, max = 150, message = "O nome do produto deve ter entre 10 e 150 caracteres")
        String nome,

        @NotNull(message = "A descrição do produto não pode ser nula")
        @Length(min = 10, max = 500, message = "A descrição do produto deve ter entre 10 e 150 caracteres")
        String descricao,

        @NotNull(message = "O produto deve ter uma imagem associada")
        String enderecoImagem,

        @Digits(fraction = 2, integer = 6, message = "O preço do produto deve ter até 8 digitos com 2 dígitos após a vírgula")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preço não pode ser negativo")
        float preco,

        @Digits(fraction = 0, integer = 6, message = "O estoque deve ter 6 dígitos sem dígitos após a vírgula")
        @DecimalMin(value = "0", inclusive = false, message = "O estoque não pode ser negativo")
        int estoque,

        @Digits(fraction = 0, integer = 6, message = "A altura do produto deve ter até 8 digitos com dois após a vírgula")
        @DecimalMin(value = "0", inclusive = false, message = "A altura do produto não pode ser negativa")
        float altura,

        @Digits(fraction = 0, integer = 6, message = "A largura do produto deve ter até 8 digitos com dois após a vírgula")
        @DecimalMin(value = "0", inclusive = false, message = "A largura do produto não pode ser negativa")
        float largura,

        @Digits(fraction = 0, integer = 6, message = "A profundidade do produto deve ter até 8 digitos com dois após a vírgula")
        @DecimalMin(value = "0", inclusive = false, message = "A profundidade do produto não pode ser negativa")
        float profundidade,

        @NotNull(message = "A condição do produto é obrigatória(NOVO ou USADO)")
        CondicaoEnum condicao
) {}
