package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Entity
@Table(name = "VENDEDORES")
@NoArgsConstructor
public class JpaVendedorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String userName;

    @Column(nullable = false, length = 50)
    private String nomeCompleto;

    @Column(nullable = false, length = 40, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String senha;

    @Column(nullable = false)
    private boolean usuarioAtivo;

    @Column(nullable = false)
    private LocalDate dataCadastro;

    @Column(nullable = false, length = 11)
    private Role role;
}
