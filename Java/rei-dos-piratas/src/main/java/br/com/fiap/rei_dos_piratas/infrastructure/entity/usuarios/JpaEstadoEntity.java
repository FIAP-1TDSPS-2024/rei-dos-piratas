package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ESTADOS")
public class JpaEstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String estadoNome;

    @Column(nullable = false, length = 2)
    private String estadoSigla;
}
