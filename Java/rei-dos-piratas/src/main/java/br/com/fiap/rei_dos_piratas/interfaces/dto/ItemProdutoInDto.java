package br.com.fiap.rei_dos_piratas.interfaces.dto;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemProdutoInDto(

        @NotNull(message = "O produto não pode ser nulo")
        Produto produto,

        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        int quantidade
){}
