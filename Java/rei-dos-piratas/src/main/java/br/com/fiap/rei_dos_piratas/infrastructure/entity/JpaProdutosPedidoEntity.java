package br.com.fiap.rei_dos_piratas.infrastructure.entity;

import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PEDIDO_PRODUTO")
public class JpaProdutosPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "produto_id")
    private JpaProdutoEntity produto;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pedido_id", nullable = false)
    private JpaPedidoEntity pedido;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    @Max(value = 999999, message = "Quantidade máxima é 999999")
    @Column(nullable = false, length = 6)
    private int quantidade;
}
