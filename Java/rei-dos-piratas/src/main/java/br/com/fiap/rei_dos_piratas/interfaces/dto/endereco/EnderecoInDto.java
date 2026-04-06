package br.com.fiap.rei_dos_piratas.interfaces.dto.endereco;

/** DTO de entrada — apenas transporta dados, sem anotações de validação. */
public record EnderecoInDto(
        int numero,
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String estadoNome,
        String estadoSigla
) {}
