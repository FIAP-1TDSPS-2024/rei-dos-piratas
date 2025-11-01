package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PEDIDO_PRODUTO")
public class JpaProdutosPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private JpaProdutoEntity produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private JpaPedidoEntity pedido;

    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    @Max(value = 999999, message = "Quantidade máxima é 999999")
    @Column(nullable = false)
    private int quantidade;
}
