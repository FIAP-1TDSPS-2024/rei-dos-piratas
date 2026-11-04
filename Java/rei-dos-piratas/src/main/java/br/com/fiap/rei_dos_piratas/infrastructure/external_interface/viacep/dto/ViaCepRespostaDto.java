package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.viacep.dto;

/** Resposta do ViaCEP ({@code /ws/{cep}/json/}); {@code erro=true} quando o CEP não existe. */
public record ViaCepRespostaDto(String cep, String logradouro, String bairro, String localidade,
                                String uf, String estado, Boolean erro) {}
