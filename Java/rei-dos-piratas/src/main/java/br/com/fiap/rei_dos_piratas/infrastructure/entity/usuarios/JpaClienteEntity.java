package br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEnderecoEntity;
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

    @Column(nullable = false, length = 11)
    private String celular;

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
                            JpaPerfilEntity perfil,
                            LocalDate dataNascimento,
                            SexoEnum sexo,
                            String cpf,
                            String celular,
                            JpaCarrinhoEntity carrinho) {
        super(id, userName, nomeCompleto, email, senha, usuarioAtivo, dataCadastro, perfil);
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.cpf = cpf;
        this.celular = celular;
        this.carrinho = carrinho;
    }
}
