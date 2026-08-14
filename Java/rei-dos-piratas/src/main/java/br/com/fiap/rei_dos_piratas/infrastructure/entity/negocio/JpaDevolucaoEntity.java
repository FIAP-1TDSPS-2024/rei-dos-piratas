package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.MotivoDevolucaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MotivoDevolucaoEnum motivo;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataSolicitacao;

    private LocalDate dataAprovacao;

    private LocalDate dataConclusao;

    private Boolean aprovada;
}

