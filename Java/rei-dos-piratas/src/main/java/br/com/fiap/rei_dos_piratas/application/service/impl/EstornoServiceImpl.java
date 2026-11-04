package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Aplica no pagamento do pedido os eventos de estorno do fornecedor. O fornecedor não diz qual estorno
 * o evento resolve: vale o mais antigo em aberto. Evento sem estorno em aberto é só registrado (sem exceção,
 * senão o evento seria reprocessado sem fim); a deduplicação por id do evento fica no EventoExterno.
 */
@Slf4j
public class EstornoServiceImpl implements EstornoService {

    private final PedidoRepository repository;

    public EstornoServiceImpl(PedidoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public void processarEventoEstorno(Long pedidoId, EventoPagamento evento) {
        log.info("[ESTORNO] Evento {} para pedido ID={}", evento.tipoOriginal(), pedidoId);

        if (evento.tipo() == TipoEventoPagamento.OUTRO_ESTORNO) {
            log.error("[ESTORNO] Contestação ou reversão {} no pedido ID={} — tratar manualmente", evento.tipoOriginal(), pedidoId);
            return;
        }

        Pedido pedido = this.repository.findById(pedidoId);
        Pagamento pagamento = pedido.getPagamento();
        Optional<Estorno> emAberto = pagamento == null ? Optional.empty() : pagamento.estornoEmAberto();
        if (emAberto.isEmpty()) {
            log.warn("[ESTORNO] Evento {} sem estorno em aberto no pedido ID={} — nada a atualizar", evento.tipoOriginal(), pedidoId);
            return;
        }

        Estorno estorno = emAberto.get();
        switch (evento.tipo()) {
            case ESTORNADA, ESTORNO_PARCIAL -> {
                pagamento.concluirEstorno(estorno);
                log.info("[ESTORNO] Estorno ID={} concluído — pagamento do pedido ID={} com status {}",
                        estorno.getId(), pedidoId, pagamento.getStatus());
            }
            case ESTORNO_EM_ANDAMENTO -> estorno.setStatus(StatusEstornoEnum.EM_ANDAMENTO);
            case ESTORNO_NEGADO -> {
                estorno.setStatus(StatusEstornoEnum.NEGADO);
                estorno.setMotivoFalha(evento.motivoRecusa());
                log.error("[ESTORNO] Estorno ID={} do pedido ID={} negado pelo fornecedor — tratar manualmente",
                        estorno.getId(), pedidoId);
            }
            default -> {
                log.warn("[ESTORNO] Evento {} sem tratamento no pedido ID={}", evento.tipoOriginal(), pedidoId);
                return;
            }
        }
        this.repository.update(pedido);
    }
}
