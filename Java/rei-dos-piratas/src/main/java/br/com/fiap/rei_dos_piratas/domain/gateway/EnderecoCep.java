package br.com.fiap.rei_dos_piratas.domain.gateway;

/**
 * Endereço oficial de um CEP. Logradouro e bairro vêm vazios no CEP geral (cidade inteira),
 * caso em que valem os informados pelo cliente.
 */
public record EnderecoCep(String cep, String logradouro, String bairro, String cidade,
                          String estadoNome, String estadoSigla) {

    /** CEP geral: o fornecedor não informa rua nem bairro. */
    public boolean geral() {
        return logradouro == null || logradouro.isBlank();
    }
}
