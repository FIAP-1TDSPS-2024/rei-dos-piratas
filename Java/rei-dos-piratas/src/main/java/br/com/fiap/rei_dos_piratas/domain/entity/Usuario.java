package br.com.fiap.rei_dos_piratas.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public abstract class Usuario {

    private Long id;

    @JsonProperty("username")
    @NotNull(message = "O nome não pode ser nulo")
    @Length(min=3, max=30, message = "O nome de usuário deve ter de 3 a 30 caracteres")
    private String userName;

    @NotNull(message = "O nome completo não deve estar nulo")
    @Length(min=5, max=50, message = "O nome completo usuário deve ter de 5 a 50 caracteres")
    private String nomeCompleto;

    @Email(message = "Insira um e-mail válido")
    @jakarta.validation.constraints.NotBlank(message = "O e-mail e obrigatorio")
    @Length(max = 40, message = "O e-mail deve ter até 20 caracteres")
    private String email;

    @NotNull(message = "A senha não pode ser nula")
    @Length(min=8, max = 20, message = "A senha deve possuir de 8 a 20 caracteres")
    private String senha;

    private boolean usuarioAtivo;

    @PastOrPresent
    @NotNull(message = "A data de cadastro do usuário não pode ser nula")
    private LocalDate dataCadastro;

    private Perfil perfil;

    @JsonIgnore
    public List<String> getPermissoes() {
        if (this.perfil == null || this.perfil.getRoles() == null) {
            return List.of();
        }

        return perfil
                .getRoles()
                .stream()
                .map(Role::getNome)
                .collect(Collectors.toList());
    }

    public String getPassword() {
        return this.senha;
    }

    public String getUsername() {
        return this.userName;
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public void setEmail(String email) {
        this.email = normalizarEmail(email);
    }

    public void validarAcesso() {
        if (!usuarioAtivo) {
            throw new br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException();
        }
    }

    public Usuario(String userName, String nomeCompleto, String email, String senha, Perfil perfil) {
        this.usuarioAtivo = true;
        this.dataCadastro = LocalDate.now();
        this.userName = userName;
        this.nomeCompleto = nomeCompleto;
        setEmail(email);
        this.senha = senha;
        this.perfil = perfil;
    }

    public Usuario(String userName, Long id, String nomeCompleto, String email, String senha,
                   boolean usuarioAtivo, LocalDate dataCadastro, Perfil perfil) {
        this.userName = userName;
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        setEmail(email);
        this.senha = senha;
        this.usuarioAtivo = usuarioAtivo;
        this.dataCadastro = dataCadastro;
        this.perfil = perfil;
    }
}
