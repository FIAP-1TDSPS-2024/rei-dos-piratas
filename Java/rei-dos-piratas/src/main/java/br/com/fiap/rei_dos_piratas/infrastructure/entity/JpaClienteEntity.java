package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CLIENTES")
public class JpaClienteEntity {
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

    @Column(nullable = false)
    private Date dataNascimento;

    @Column(nullable = false)
    private SexoEnum sexo;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL)
    private JpaEnderecoEntity endereco;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    public JpaClienteEntity(Long clienteId) {
        this.id = clienteId;
    }
}
