package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;

@Getter
public class Cliente extends Usuario{

    @Past
    @NotNull(message = "A data de nascimento não pode ser nula")
    private LocalDate dataNascimento;

    @NotNull(message = "O sexo do usuário não pode ser nulo")
    private SexoEnum sexo;

    @NotNull(message = "O endereço do usuário não pode ser nulo")
    private Endereco endereco;

    @Pattern(regexp = "[0-9]{11}", message = "O CPF do usuário deve ter 11 dígitos, sendo apenas números")
    private String cpf;

    public Cliente(
            String userName,
            String nomeCompleto,
            String email,
            String senha,
            LocalDate dataNascimento,
            SexoEnum sexo,
            Endereco endereco,
            String cpf) {
        super(userName, nomeCompleto, email, senha);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.cpf = cpf;
    }

    public Cliente(
                   Long id,
                   String userName,
                   String nomeCompleto,
                   String email, String senha,
                   boolean usuarioAtivo,
                   LocalDate dataCadastro,
                   LocalDate dataNascimento,
                   SexoEnum sexo,
                   Endereco endereco,
                   String cpf) {
        super(userName, id, nomeCompleto, email, senha, usuarioAtivo, dataCadastro);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.cpf = cpf;
    }
}
