package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DadosEmpresa {

    private Long id;

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;

    private String email;
    private String telefone;
    private String dominio;

    // Dados fiscais
    private String stateAbbr;              // UF
    private String stateRegister;           // Inscrição Estadual
    private String economicActivityCode;    // CNAE
}
