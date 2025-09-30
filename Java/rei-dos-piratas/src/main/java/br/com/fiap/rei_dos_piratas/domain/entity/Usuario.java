package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

@Getter
public abstract class Usuario {

    private Long id;

    @NotNull(message = "O nome não pode ser nulo")
    @Length(max=30, message = "O nome de usuário deve ter de 3 a 30 caracteres")
    private String userName;

    @NotNull(message = "O nome completo não deve estar nulo")
    @Length(min=5, max=50, message = "O nome completo usuário deve ter de 5 a 50 caracteres")
    private String nomeCompleto;

    @Email(message = "Insira um e-mail válido")
    @Length(max = 40, message = "O e-mail deve ter até 20 caracteres")
    private String email;

    @NotNull(message = "A senha não pode ser nula")
    @Length(min=8, max = 20, message = "A senha deve possuir de 8 a 20 caracteres")
    private String senha;

    private boolean usuarioAtivo;

    @Past
    @NotNull(message = "A data de cadastro do usuário não pode ser nula")
    private LocalDate dataCadastro;

    public Usuario(String userName, String email, String senha, Long id) {

        this.usuarioAtivo = true;
        this.dataCadastro = LocalDate.now();

        this.userName = userName;
        this.email = email;
        this.senha = senha;
        this.id = id;
    }

    public Usuario(String userName, Long id, String nomeCompleto, String email, String senha, boolean usuarioAtivo, LocalDate dataCadastro) {
        this.userName = userName;
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.senha = senha;
        this.usuarioAtivo = usuarioAtivo;
        this.dataCadastro = dataCadastro;
    }
}
