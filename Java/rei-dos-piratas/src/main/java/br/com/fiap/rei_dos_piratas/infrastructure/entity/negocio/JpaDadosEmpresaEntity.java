package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEnderecoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

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
    @Length(min = 14, max = 14)
    private String cnpj;

    private String email;
    private String telefone;
    private String dominio;

    // Dados fiscais
    private String stateAbbr;              // UF
    private String stateRegister;           // Inscrição Estadual
    private String economicActivityCode;

    @OneToOne(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = false)
    private JpaEnderecoEntity endereco;

    public JpaDadosEmpresaEntity(Long id, String razaoSocial, String nomeFantasia, String cnpj, String email, String telefone, String dominio, String stateAbbr, String stateRegister, String economicActivityCode) {
        this.razaoSocial = razaoSocial;
        this.id = id;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.email = email;
        this.telefone = telefone;
        this.dominio = dominio;
        this.stateAbbr = stateAbbr;
        this.stateRegister = stateRegister;
        this.economicActivityCode = economicActivityCode;
    }
}
