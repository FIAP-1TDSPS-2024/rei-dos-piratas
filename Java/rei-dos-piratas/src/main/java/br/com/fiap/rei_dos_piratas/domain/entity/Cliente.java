package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Usuario{

    @Past
    @NotNull(message = "A data de nascimento não pode ser nula")
    private LocalDate dataNascimento;

    @NotNull(message = "O sexo do usuário não pode ser nulo")
    private SexoEnum sexo;

    @CPF(message = "Insira um CPF válido")
    private String cpf;

    @Pattern(regexp = "[0-9]{11}", message = "O celular do usuário deve ter 11 dígitos com DDD, sendo apenas números")
    private String celular;

    @NotNull(message = "O cliente deve possuir um carrinho")
    private Carrinho carrinho;

    public Cliente(
            String userName,
            String nomeCompleto,
            String email,
            String senha,
            LocalDate dataNascimento,
            SexoEnum sexo,
            String cpf,
            String celular) {
        super(userName, nomeCompleto, email, senha, Role.CLIENT);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.cpf = cpf;
        this.celular = celular;
        this.carrinho = new Carrinho(
                null,
                new ArrayList<ItemProdutoCarrinho>()
        );
    }

    public Cliente(
                   Long id,
                   String userName,
                   String nomeCompleto,
                   String email,
                   String senha,
                   boolean usuarioAtivo,
                   LocalDate dataCadastro,
                   LocalDate dataNascimento,
                   SexoEnum sexo,
                   String cpf,
                   String celular,
                   Carrinho carrinho) {
        super(userName, id, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, Role.CLIENT);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.cpf = cpf;
        this.celular = celular;
        this.carrinho = carrinho;
    }
}
