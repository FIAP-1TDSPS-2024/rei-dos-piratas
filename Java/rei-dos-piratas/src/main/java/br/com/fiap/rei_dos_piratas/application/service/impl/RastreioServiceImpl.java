package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.DevolucaoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.RastreioService;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class RastreioServiceImpl implements RastreioService {

    private final PedidoService pedidoService;

    private final DevolucaoService devolucaoService;

    public RastreioServiceImpl(PedidoService pedidoService, DevolucaoService devolucaoService) {
        this.pedidoService = pedidoService;
        this.devolucaoService = devolucaoService;
    }

    @Override
    public void processarEvento(EventoRastreio evento) {
        log.info("Processando evento de rastreio: event={}, envio={}", evento.tipoOriginal(), evento.envio());

        Optional<Pedido> pedido = this.pedidoService.findByPedidoFrete(evento.envio());
        if (pedido.isPresent()) {
            log.debug("Envio do pedido ID={} — status atual={}", pedido.get().getId(), pedido.get().getStatus());
            this.pedidoService.rastreioPedidoWebhook(pedido.get(), evento);
            return;
        }

        Optional<Devolucao> devolucao = this.devolucaoService.findByPedidoFrete(evento.envio());
        if (devolucao.isPresent()) {
            log.debug("Envio da devolução ID={} — status atual={}", devolucao.get().getId(), devolucao.get().getStatus());
            this.devolucaoService.rastreioDevolucaoWebhook(devolucao.get(), evento);
            return;
        }

        log.info("Nenhum pedido ou devolução para o envio {} no evento {}", evento.envio(), evento.tipoOriginal());
    }
}
