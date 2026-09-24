package br.com.fiap.rei_dos_piratas.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotivoDevolucao {

    private Long id;

    private String codigo;

    private String descricao;

    private Boolean arrependimento;
}

