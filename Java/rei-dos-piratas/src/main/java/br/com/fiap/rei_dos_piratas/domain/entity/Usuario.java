package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public abstract class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private boolean usuarioAtivo;
    private Date dataCadastro;
}
