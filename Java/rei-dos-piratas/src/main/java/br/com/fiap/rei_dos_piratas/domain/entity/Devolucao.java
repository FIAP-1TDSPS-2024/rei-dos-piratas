package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.MotivoDevolucaoEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Devolucao {

    private Long id;

    @NotNull(message = "O pedido da devolução não pode ser nulo")
    private Pedido pedido;

    @NotNull(message = "O motivo da devolução não pode ser nulo")
    private MotivoDevolucaoEnum motivo;

    @Size(max = 500, message = "A descrição da devolução deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "A data da solicitação não pode ser nula")
    @PastOrPresent(message = "A data de solicitação deve estar no presente ou passado")
    private LocalDate dataSolicitacao;

    @PastOrPresent(message = "A data de aprovação deve estar no presente ou passado")
    private LocalDate dataAprovacao;

    @PastOrPresent(message = "A data de conclusão deve estar no presente ou passado")
    private LocalDate dataConclusao;

    private Boolean aprovada;

    public Devolucao(Pedido pedido, MotivoDevolucaoEnum motivo, String descricao) {
        this.pedido = pedido;
        this.motivo = motivo;
        this.descricao = descricao;
        this.dataSolicitacao = LocalDate.now();
        this.aprovada = null;
    }
}
