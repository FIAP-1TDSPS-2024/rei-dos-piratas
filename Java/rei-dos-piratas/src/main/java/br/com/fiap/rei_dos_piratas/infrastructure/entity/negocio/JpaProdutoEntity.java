package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaFuncionarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRODUTOS")
public class JpaProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false)
    private String enderecoImagem;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    private BigDecimal altura;

    private BigDecimal largura;

    private BigDecimal profundidade;

    private BigDecimal peso;

    @Column(nullable = false, length = 255)
    private CondicaoEnum condicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private JpaFuncionarioEntity funcionario;
}
