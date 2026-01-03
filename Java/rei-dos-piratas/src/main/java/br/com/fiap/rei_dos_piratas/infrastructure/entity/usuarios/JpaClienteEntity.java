package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaPedidoEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CLIENTES")
public class JpaClienteEntity extends JpaUsuarioEntity{
    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private SexoEnum sexo;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<JpaEnderecoEntity> enderecos;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private JpaCarrinhoEntity carrinho;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaPedidoEntity> pedidos;

    public JpaClienteEntity(Long id,
                            String userName,
                            String nomeCompleto,
                            String email,
                            String senha,
                            boolean usuarioAtivo,
                            LocalDate dataCadastro,
                            LocalDate dataNascimento,
                            SexoEnum sexo,
                            String cpf,
                            JpaCarrinhoEntity carrinho) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, Role.CLIENT);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.cpf = cpf;
        this.carrinho = carrinho;
    }
}
