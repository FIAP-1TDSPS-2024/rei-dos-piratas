package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "FUNCIONARIOS")
@NoArgsConstructor
public class JpaFuncionarioEntity extends JpaUsuarioEntity {

    private LocalDate dataDemissao;

    private float salario;

    public JpaFuncionarioEntity(Long id,
                                String userName,
                                String nomeCompleto,
                                String email,
                                String senha,
                                boolean usuarioAtivo,
                                LocalDate dataCadastro,
                                Role role,
                                LocalDate dataDemissao,
                                float salario) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, role);
        this.dataDemissao = dataDemissao;
        this.salario = salario;
    }
}
