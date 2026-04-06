package br.com.fiap.rei_dos_piratas.interfaces.dto.endereco;

/** DTO de saída — apenas transporta dados, sem anotações de validação. */
public record EnderecoOutDto(
        Long id,
        int numero,
        String cep,
        String logradouro,
        String bairro,
        Long cidadeId,
        String cidade,
        Long estadoId,
        String estadoNome,
        String estadoSigla
) {}
