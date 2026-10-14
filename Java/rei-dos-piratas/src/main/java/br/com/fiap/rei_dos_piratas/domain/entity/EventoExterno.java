package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusProcessamentoEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class EventoExterno {

    private Long id;
    private final OrigemEventoEnum origem;
    private final String idExterno;
    private final String tipoEvento;
    private final LocalDateTime dataRecebimento;
    private StatusProcessamentoEventoEnum statusProcessamento;
    private final String payload;
    private int tentativas;
    private LocalDateTime processandoAte;

    public EventoExterno(OrigemEventoEnum origem, String idExterno, String tipoEvento, String payload) {
        if (origem == null) {
            throw new RegraDeNegocioException("A origem do evento é obrigatória");
        }
        if (idExterno == null || idExterno.isBlank()) {
            throw new RegraDeNegocioException("O ID externo do evento é obrigatório");
        }
        if (tipoEvento == null || tipoEvento.isBlank()) {
            throw new RegraDeNegocioException("O tipo do evento é obrigatório");
        }
        this.origem = origem;
        this.idExterno = idExterno;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.dataRecebimento = LocalDateTime.now();
        this.statusProcessamento = StatusProcessamentoEventoEnum.PENDENTE;
        this.tentativas = 0;
    }

    public EventoExterno(Long id,
                         OrigemEventoEnum origem,
                         String idExterno,
                         String tipoEvento,
                         LocalDateTime dataRecebimento,
                         StatusProcessamentoEventoEnum statusProcessamento,
                         String payload,
                         int tentativas,
                         LocalDateTime processandoAte) {
        this.id = id;
        this.origem = origem;
        this.idExterno = idExterno;
        this.tipoEvento = tipoEvento;
        this.dataRecebimento = dataRecebimento;
        this.statusProcessamento = statusProcessamento;
        this.payload = payload;
        this.tentativas = tentativas;
        this.processandoAte = processandoAte;
    }

    /**
     * Registra uma nova tentativa de processamento, reservando o evento por um prazo (lease).
     * Um PROCESSANDO com lease expirado é considerado abandonado e pode ser assumido.
     *
     * @return false se o evento já foi processado ou outra instância o está processando dentro do lease
     */
    public boolean iniciarProcessamento(LocalDateTime agora, Duration lease) {
        if (statusProcessamento == StatusProcessamentoEventoEnum.PROCESSADO) {
            return false;
        }
        if (statusProcessamento == StatusProcessamentoEventoEnum.PROCESSANDO
                && processandoAte != null && processandoAte.isAfter(agora)) {
            return false;
        }
        this.statusProcessamento = StatusProcessamentoEventoEnum.PROCESSANDO;
        this.processandoAte = agora.plus(lease);
        this.tentativas++;
        return true;
    }

    public void concluir() {
        this.statusProcessamento = StatusProcessamentoEventoEnum.PROCESSADO;
        this.processandoAte = null;
    }

    public void falhar() {
        this.statusProcessamento = StatusProcessamentoEventoEnum.FALHA;
        this.processandoAte = null;
    }
}
