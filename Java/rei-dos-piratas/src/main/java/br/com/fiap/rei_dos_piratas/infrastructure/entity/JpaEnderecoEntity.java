package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ENDERECO_CLIENTES")
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

    @Column(nullable = false, length = 50)
    private String cidade;

    @Column(nullable = false, length = 30)
    private String estadoNome;

    @Column(nullable = false, length = 2)
    private String estadoSigla;

    @Column(nullable = false, length = 50)
    private String paisNome;

    @Column(nullable = false, length = 3)
    private String paisSigla;

    @OneToOne(fetch = FetchType.LAZY)
    private JpaClienteEntity cliente;
}
