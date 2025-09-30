package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class JpaVendedorEntity {
    private Long id;
    private String userName;
    private String nomeCompleto;
    private String email;
    private String senha;
    private boolean usuarioAtivo;
    private LocalDate dataCadastro;
    private Role role;
}
