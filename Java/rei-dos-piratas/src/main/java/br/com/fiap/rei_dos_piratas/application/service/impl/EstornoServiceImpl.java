package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.EstornoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EstornoServiceImpl implements EstornoService {

    private final PedidoService pedidoService;

    private final DevolucaoService devolucaoService;

    public EstornoServiceImpl(PedidoService pedidoService, DevolucaoService devolucaoService) {
        this.pedidoService = pedidoService;
        this.devolucaoService = devolucaoService;
    }

    @Override
    public void processarEventoEstorno(Long pedidoId, EventoCobrancaDto evento) {
        log.info("Processando evento de estorno para pedido ID={} — evento={}", pedidoId, evento.event());

        Pedido pedido = pedidoService.findById(pedidoId);

        if (pedido.getStatus().equals(StatusEnum.CANCELADO)) {
            tratarEvento(pedidoId, evento, OrigemEstornoEnum.CANCELAMENTO, null);
        }
        else {
            List<Devolucao> devolucoes = devolucaoService.findAllByPedidoId(pedidoId);
            if (devolucoes.isEmpty()) {
                log.info("Nenhuma devolução encontrada para o pedido ID={} — evento={}", pedidoId, evento.event());
            } else {
                Optional<Devolucao> ultimaDevolucao = devolucoes.stream()
                        .filter(d -> d.getDataConclusao() != null)
                        .max(Comparator.comparing(Devolucao::getDataConclusao));

                if (ultimaDevolucao.isEmpty()) {
                    log.info("Nenhuma devolução retornada para o pedido ID={}", pedidoId);
                } else {
                    Devolucao devolucao = ultimaDevolucao.get();
                    tratarEvento(pedidoId, evento, OrigemEstornoEnum.DEVOLUCAO, devolucao.getId());
                }
            }
        }
    }

    private void tratarEvento(Long pedidoId, EventoCobrancaDto evento, OrigemEstornoEnum origem, Long devolucaoId) {
        log.info("Estorno [origem={}] pedido ID={} devolução ID={} — evento={}", origem, pedidoId, devolucaoId, evento.event());

        switch (evento.event()) {
            case "PAYMENT_REFUNDED":
                log.info("Estorno feito com sucesso [origem={}] pedido ID={} devolução ID={}", origem, pedidoId, devolucaoId);
                break;
            case "PAYMENT_PARTIALLY_REFUNDED":
                log.info("Estorno parcial feito com sucesso [origem={}] pedido ID={} devolução ID={}", origem, pedidoId, devolucaoId);
                break;
            case "PAYMENT_REFUND_IN_PROGRESS":
                log.info("Estorno em progresso [origem={}] pedido ID={} devolução ID={}", origem, pedidoId, devolucaoId);
                break;
            case "PAYMENT_REFUND_DENIED":
                log.info("Estorno negado [origem={}] pedido ID={} devolução ID={} — motivo: {}", origem, pedidoId, devolucaoId, evento.additionalInfo().denialReason());
                break;
            default:
                log.warn("Evento desconhecido [origem={}] pedido ID={} devolução ID={} — evento={}", origem, pedidoId, devolucaoId, evento.event());
        }
    }
}
