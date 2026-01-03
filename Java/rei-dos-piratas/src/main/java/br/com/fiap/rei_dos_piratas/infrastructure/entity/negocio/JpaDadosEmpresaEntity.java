package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DADOS_EMPRESA")
public class JpaDadosEmpresaEntity {
    @Id
    private Long id = 1L; // sempre 1 registro

    private String razaoSocial;

    private String nomeFantasia;

    @Column(length = 14)
    private String cnpj;

    private String email;

    private String telefone;

    private String dominio;

    @OneToOne(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = false)
    private JpaEnderecoEntity endereco;

    public JpaDadosEmpresaEntity(Long id, String razaoSocial, String nomeFantasia, String cnpj, String email, String telefone, String dominio) {
        this.id = id;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.email = email;
        this.telefone = telefone;
        this.dominio = dominio;
    }
}
