package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DEVOLUCOES")
public class JpaDevolucaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private JpaPedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivo_devolucao_id", nullable = false)
    private JpaMotivoDevolucaoEntity motivo;

    @Column(length = 500)
    private String descricao;

    @OneToMany(mappedBy = "devolucao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<JpaItemDevolucaoEntity> itens;

    private BigDecimal valorTotal;

    private BigDecimal valorFrete;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatusEnum status;

    @Column(nullable = false)
    private LocalDate dataSolicitacao;

    private LocalDate dataAprovacao;

    private LocalDate dataConclusao;

    private Boolean aprovada;

    private Long servicoEntrega;

    private UUID pedidoFrete;

    private String protocoloEnvio;

    private String statusEnvio;

    private String tracking;

    private String trackingUrl;
}

