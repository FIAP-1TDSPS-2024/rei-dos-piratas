package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "FUNCIONARIOS")
@NoArgsConstructor
public class JpaFuncionarioEntity extends JpaUsuarioEntity {

    private LocalDate dataDemissao;

    private BigDecimal salario;

    public JpaFuncionarioEntity(Long id,
                                String userName,
                                String nomeCompleto,
                                String email,
                                String senha,
                                boolean usuarioAtivo,
                                LocalDate dataCadastro,
                                JpaPerfilEntity perfil,
                                LocalDate dataDemissao,
                                BigDecimal salario) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, perfil);
        this.dataDemissao = dataDemissao;
        this.salario = salario;
    }
}
