package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Date;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "CLIENTES")
public class JpaClienteEntity extends JpaUsuarioEntity{
    @Setter
    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Setter
    @Column(nullable = false)
    private SexoEnum sexo;

    @Setter
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL)
    private JpaEnderecoEntity endereco;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    public JpaClienteEntity(Long id,
                            String userName,
                            String nomeCompleto,
                            String email,
                            String senha,
                            boolean usuarioAtivo,
                            LocalDate dataCadastro,
                            LocalDate dataNascimento,
                            SexoEnum sexo,
                            JpaEnderecoEntity endereco,
                            String cpf) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.cpf = cpf;
    }
}
