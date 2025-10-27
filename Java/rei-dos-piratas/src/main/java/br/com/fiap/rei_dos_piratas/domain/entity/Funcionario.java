package br.com.fiap.rei_dos_piratas.domain.entity;


import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class Funcionario extends Usuario{

    @PastOrPresent
    LocalDate dataDemissao;

    @Digits(fraction = 2, integer = 6, message = "O salario deve ter até 8 digitos com 2 dígitos após a vírgula")
    @DecimalMin(value = "0.0", message = "O salario não pode ser negativo")
    float salario;

    public Funcionario(String userName,
                       String nomeCompleto,
                       String email,
                       String senha,
                       Role role,
                       float salario) {
        super(userName, nomeCompleto, email, senha, role);
        this.salario = salario;
    }

    public Funcionario(String userName,
                       Long id,
                       String nomeCompleto,
                       String email,
                       String senha,
                       boolean usuarioAtivo,
                       LocalDate dataCadastro,
                       Role role,
                       LocalDate dataDemissao,
                       float salario) {
        super(userName, id, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, role);
        this.dataDemissao = dataDemissao;
        this.salario = salario;
    }
}
