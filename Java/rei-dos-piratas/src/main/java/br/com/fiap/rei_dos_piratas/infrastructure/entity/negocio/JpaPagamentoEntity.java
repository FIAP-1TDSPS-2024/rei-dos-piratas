package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PAGAMENTOS")
public class JpaPagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private JpaPedidoEntity pedido;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoPagamentoEnum tipo;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusPagamentoEnum status;

    @Column(length = 50)
    private String idCobranca;

    @Column(nullable = false)
    private BigDecimal valor;

    private BigDecimal valorLiquido;

    @Column(nullable = false)
    private Integer parcelas;

    // Sem cascade: o cartao e gravado pelo CartaoClienteRepository; aqui so vai a FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartao_cliente_id")
    private JpaCartaoClienteEntity cartao;

    private String motivoRecusa;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataConfirmacao;

    @Column(length = 500)
    private String urlBoleto;

    private java.time.LocalDate dataVencimento;

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private java.util.List<JpaEstornoEntity> estornos = new java.util.ArrayList<>();

    public JpaPagamentoEntity(Long id, JpaPedidoEntity pedido, TipoPagamentoEnum tipo, StatusPagamentoEnum status,
                              String idCobranca, BigDecimal valor, BigDecimal valorLiquido, Integer parcelas,
                              JpaCartaoClienteEntity cartao, String motivoRecusa, LocalDateTime dataCriacao,
                              LocalDateTime dataConfirmacao, String urlBoleto, java.time.LocalDate dataVencimento) {
        this.id = id;
        this.pedido = pedido;
        this.tipo = tipo;
        this.status = status;
        this.idCobranca = idCobranca;
        this.valor = valor;
        this.valorLiquido = valorLiquido;
        this.parcelas = parcelas;
        this.cartao = cartao;
        this.motivoRecusa = motivoRecusa;
        this.dataCriacao = dataCriacao;
        this.dataConfirmacao = dataConfirmacao;
        this.urlBoleto = urlBoleto;
        this.dataVencimento = dataVencimento;
    }
}
