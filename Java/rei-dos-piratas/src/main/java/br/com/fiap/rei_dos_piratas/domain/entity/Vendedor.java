package br.com.fiap.rei_dos_piratas.domain.entity;


import br.com.fiap.rei_dos_piratas.domain.Enum.Role;

import java.util.Date;

public class Vendedor extends Usuario{

    private Role role;

    public Vendedor(Long usuarioId, String usuarioNome, String usuarioEmail, String usuarioSenha, boolean usuarioAtivo, Date usuarioDataCadastro, Role role) {
        super(usuarioId, usuarioNome, usuarioEmail, usuarioSenha, usuarioAtivo, usuarioDataCadastro);
        this.role = role;
    }
}
