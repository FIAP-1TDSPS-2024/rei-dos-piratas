package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Cidade {

    Long id;

    String nome;

    Estado estado;
}
