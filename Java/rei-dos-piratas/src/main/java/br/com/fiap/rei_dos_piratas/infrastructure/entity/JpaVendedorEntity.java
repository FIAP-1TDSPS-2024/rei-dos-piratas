package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "VENDEDORES")
@NoArgsConstructor
public class JpaVendedorEntity extends JpaUsuarioEntity{
    @Column(nullable = false, length = 11)
    private Role role;

    public JpaVendedorEntity(Long id,
                             String userName,
                             String nomeCompleto,
                             String email,
                             String senha,
                             boolean usuarioAtivo,
                             LocalDate dataCadastro,
                             Role role) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro);
        this.role = role;
    }
}
