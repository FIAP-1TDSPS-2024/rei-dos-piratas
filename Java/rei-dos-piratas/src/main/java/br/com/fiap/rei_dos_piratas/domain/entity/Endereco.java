package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Endereco {
    private Long id;
    private int numero;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String estadoNome;
    private String estadoSigla;
    private String paisNome;
    private String paisSigla;

    @Override
    public String toString() {
        return logradouro + ","
                + bairro + ","
                + cidade + " - "
                + estadoNome
                + "/"
                + estadoSigla
                + " - "
                + paisNome;
    }
}
