package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CARRINHO_PRODUTO")
public class JpaProdutosCarrinhoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "produto_id")
    private JpaProdutoEntity produto;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private JpaCarrinhoEntity carrinho;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    @Max(value = 999999, message = "Quantidade máxima é 999999")
    @Column(nullable = false)
    private int quantidade;
}
