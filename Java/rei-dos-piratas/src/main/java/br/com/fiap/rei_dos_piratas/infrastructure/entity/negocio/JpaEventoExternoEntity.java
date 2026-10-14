package br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusProcessamentoEventoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EVENTOS_EXTERNOS",
        uniqueConstraints = @UniqueConstraint(name = "uk_eventos_externos_origem_id_externo", columnNames = {"origem", "idExterno"}))
public class JpaEventoExternoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemEventoEnum origem;

    @Column(nullable = false, length = 100)
    private String idExterno;

    @Column(nullable = false, length = 60)
    private String tipoEvento;

    @Column(nullable = false)
    private LocalDateTime dataRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProcessamentoEventoEnum statusProcessamento;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(nullable = false)
    private Integer tentativas;

    private LocalDateTime processandoAte;
}
