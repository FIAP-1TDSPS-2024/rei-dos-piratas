package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DEVOLUCAO_ITENS")
public class JpaItemDevolucaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devolucao_id", nullable = false)
    private JpaDevolucaoEntity devolucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_produto_id", nullable = false)
    private JpaProdutosPedidoEntity itemPedido;

    @Column(nullable = false)
    private Integer quantidade;
}

