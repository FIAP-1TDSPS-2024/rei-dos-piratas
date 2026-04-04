package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PERFIS")
public class JpaPerfilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(min = 3, max = 30, message = "O nome do perfil deve ter de 3 a 30 caracteres")
    @Column(nullable = false, length = 30, unique = true)
    private String nome;

    @Column
    private String descricao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "PERFIL_ROLES",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<JpaRoleEntity> roles;

    @OneToMany(mappedBy = "perfil", orphanRemoval = false)
    private List<JpaUsuarioEntity> usuarios;
}
