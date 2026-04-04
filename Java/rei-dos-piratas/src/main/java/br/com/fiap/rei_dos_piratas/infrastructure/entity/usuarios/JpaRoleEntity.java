package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ROLES")
public class JpaRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 3, max = 50, message = "O nome da role deve ter de 3 a 50 caracteres")
    @Column(nullable = false, length = 50, unique = true)
    private String nome;

    @Column
    private String descricao;
}
