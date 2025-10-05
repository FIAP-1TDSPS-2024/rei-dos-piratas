package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
@AllArgsConstructor
public class Endereco {
    private Long id;
    @Digits(integer = 5, fraction = 0, message = "O número deve possuir até 5 dígitos")
    private int numero;
    @Pattern(regexp = "[0-9]{8}", message = "O CEP deve ter 8 dígitos, sendo apenas números")
    private String cep;
    @Length(max = 70, message = "O logradouro deve possuir até 70 algarismos")
    private String logradouro;
    @Length(max = 50, message = "O bairro deve possuir até 50 caracteres")
    private String bairro;
    private Long cidadeId;
    @NotNull(message = "O nome da cidade não pode ser nulo")
    @Length(max = 50, message = "O nome da cidade deve possuir até 50 caracteres")
    private String cidade;
    private Long estadoId;
    @NotNull(message = "O nome do estado não pode ser nulo")
    @Length(max = 50, message = "O nome do estado deve possuir até 50 caracteres")
    private String estadoNome;
    @NotNull(message = "A sigla do estado não pode estar nula")
    @Length(min = 2, max = 2, message = "A sigla do estado deve possuir 2 dígitos")
    private String estadoSigla;
    private String paisNome;
    private String paisSigla;

    @Override
    public String toString() {
        return logradouro + ","
                + bairro + ","
                + cidade + " - "
                + estadoNome
                + "/"
                + estadoSigla
                + " - "
                + paisNome;
    }
}
