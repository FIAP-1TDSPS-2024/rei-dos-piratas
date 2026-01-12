package br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaDadosEmpresaEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "ENDERECO_ATIVO = 1")
@Entity
@Table(name = "ENDERECO")
public class JpaEnderecoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5)
    private int numero;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 70)
    private String logradouro;

    @Column(nullable = false, length = 50)
    private String bairro;

    private boolean enderecoAtivo;

    @ManyToOne()
    @JoinColumn(name = "cidade_id")
    private JpaCidadeEntity cidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private JpaClienteEntity cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private JpaDadosEmpresaEntity empresa;

    public JpaEnderecoEntity(Long id, int numero, String cep, String logradouro, String bairro, Boolean enderecoAtivo, JpaCidadeEntity cidade, JpaClienteEntity cliente) {
        this.id = id;
        this.numero = numero;
        this.cep = cep;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.enderecoAtivo = enderecoAtivo;
        this.cidade = cidade;
        this.cliente = cliente;
    }
}
