package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ESTORNOS")
public class JpaEstornoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id", nullable = false)
    private JpaPagamentoEntity pagamento;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrigemEstornoEnum origem;

    // Só a FK: a devolução é outro agregado
    @Column(name = "devolucao_id")
    private Long devolucaoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private StatusEstornoEnum status;

    @Column(length = 500)
    private String urlSolicitacaoBoleto;

    private String motivoFalha;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataConclusao;
}
