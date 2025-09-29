package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;

import java.util.Date;

public class Cliente extends Usuario{

    private Date dataNascimento;
    private SexoEnum sexo;
    private Endereco endereco;
    private String cpf;

    public Cliente(Long id, String nome, String email, String senha, boolean usuarioAtivo, Date dataCadastro, Date dataNascimento, SexoEnum sexo, Endereco endereco, String cpf) {
        super(id, nome, email, senha, usuarioAtivo, dataCadastro);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.cpf = cpf;
    }
}
