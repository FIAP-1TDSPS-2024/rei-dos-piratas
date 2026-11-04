package br.com.fiap.rei_dos_piratas.interfaces.dto.endereco;

/** Endereço oficial do CEP; {@code geral=true} quando o cliente precisa informar rua e bairro. */
public record CepOutDto(String cep, String logradouro, String bairro, String cidade,
                        String estadoNome, String estadoSigla, boolean geral) {}
