package br.com.fiap.rei_dos_piratas.domain.entity;


import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDate;
import java.util.Date;

@Getter
public class Vendedor extends Usuario{

    private Role role;

    public Vendedor(String userName,
                    String email,
                    String senha,
                    Long id,
                    Role role) {
        super(userName, email, senha, id);
        if(role == null){
            this.role = Role.USER;
        }
        else{
            this.role = role;
        }
    }

    public Vendedor(String userName,
                    Long id,
                    String nomeCompleto,
                    String email,
                    String senha,
                    boolean usuarioAtivo,
                    LocalDate dataCadastro,
                    Role role) {
        super(userName, id, nomeCompleto, email, senha, usuarioAtivo, dataCadastro);
        if(role == null){
            this.role = Role.USER;
        }
        else{
            this.role = role;
        }
    }
}
