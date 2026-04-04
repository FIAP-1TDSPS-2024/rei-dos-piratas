package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Perfil {

    private Long id;

    @Length(min = 3, max = 30, message = "O nome do perfil deve ter de 3 a 30 caracteres")
    private String nome;

    private String descricao;

}
