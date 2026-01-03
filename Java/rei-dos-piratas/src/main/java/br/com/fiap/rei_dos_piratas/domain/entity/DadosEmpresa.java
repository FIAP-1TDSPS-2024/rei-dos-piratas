package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DadosEmpresa {
    private Long id = 1L; // sempre 1 registro

    private String razaoSocial;

    private String nomeFantasia;

    private String cnpj;

    private String email;

    private String telefone;

    private String dominio;
}
