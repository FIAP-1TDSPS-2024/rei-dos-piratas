package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ESTADOS")
public class JpaEstadoEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 30)
    private String estadoNome;

    @Column(nullable = false, length = 2)
    private String estadoSigla;

    @ManyToOne
    private JpaPaisEntity pais;
}
