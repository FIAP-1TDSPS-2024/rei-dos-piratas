package br.com.fiap.rei_dos_piratas.domain.entity;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Cidade cidade;
    private String paisNome;
    private String paisSigla;
    private Cliente cliente;

    @Override
    public String toString() {
        return logradouro + ","
                + bairro + ","
                + cep + " - "
                + paisNome;
    }
}
