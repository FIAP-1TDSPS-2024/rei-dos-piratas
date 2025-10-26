package br.com.fiap.rei_dos_piratas.domain.entity;


import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class Funcionario extends Usuario{



    public Funcionario(String userName,
                       String nomeCompleto,
                       String email,
                       String senha,
                       Role role) {
        super(userName, nomeCompleto, email, senha, role);
    }

    public Funcionario(String userName,
                       Long id,
                       String nomeCompleto,
                       String email,
                       String senha,
                       boolean usuarioAtivo,
                       LocalDate dataCadastro,
                       Role role) {
        super(userName, id, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, role);
    }
}
