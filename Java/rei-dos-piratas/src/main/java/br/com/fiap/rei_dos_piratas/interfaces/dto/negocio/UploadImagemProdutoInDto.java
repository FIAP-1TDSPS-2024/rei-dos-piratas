package br.com.fiap.rei_dos_piratas.interfaces.dto.negocio;

import jakarta.validation.constraints.NotBlank;

/** Pedido de URL de upload: o tipo do arquivo escolhido (o formato aceito é regra do domínio). */
public record UploadImagemProdutoInDto(
        @NotBlank(message = "Informe o tipo do arquivo (content_type).")
        String contentType
) {}
