package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    private Long id;

    @Length(min = 3, max = 50, message = "O nome da role deve ter de 3 a 50 caracteres")
    private String nome;

    private String descricao;

    private List<Perfil> perfis;
}
