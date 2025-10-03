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

    @ManyToOne
    private JpaCidadeEntity cidade;

    @OneToOne(fetch = FetchType.LAZY)
    private JpaClienteEntity cliente;
}
