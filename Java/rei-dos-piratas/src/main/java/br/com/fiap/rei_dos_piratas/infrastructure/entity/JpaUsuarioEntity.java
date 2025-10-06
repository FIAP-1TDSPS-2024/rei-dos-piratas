package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JpaUsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Setter
    @Column(nullable = false, length = 30, unique = true)
    private String userName;

    @Setter
    @Column(nullable = false, length = 50)
    private String nomeCompleto;

    @Setter
    @Column(nullable = false, length = 40, unique = true)
    private String email;

    @Setter
    @Column(nullable = false, length = 20)
    private String senha;

    @Setter
    @Column(nullable = false)
    private boolean usuarioAtivo;

    @Setter
    @Column(nullable = false)
    private LocalDate dataCadastro;
}
