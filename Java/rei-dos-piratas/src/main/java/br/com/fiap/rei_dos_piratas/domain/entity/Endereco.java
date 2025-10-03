package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Endereco {
    private Long id;
    private int numero;
    private String cep;
    private String logradouro;
    private String bairro;
    private Long cidadeId;
    private String cidade;
    private Long estadoId;
    private String estadoNome;
    private String estadoSigla;
    private Long paisId;
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
