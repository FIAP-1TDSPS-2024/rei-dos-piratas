package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PAISES")
public class JpaPaisEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String paisNome;

    @Column(nullable = false, length = 3)
    private String paisSigla;

}
