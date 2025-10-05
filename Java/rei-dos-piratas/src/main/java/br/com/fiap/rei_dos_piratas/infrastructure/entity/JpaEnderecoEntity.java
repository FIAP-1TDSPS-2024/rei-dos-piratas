package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(cascade = CascadeType.PERSIST)
    private JpaCidadeEntity cidade;

    @OneToOne
    private JpaClienteEntity cliente;
}
