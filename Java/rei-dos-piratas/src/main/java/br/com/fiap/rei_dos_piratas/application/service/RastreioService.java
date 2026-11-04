package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio;

/** Atualiza o pedido ou a devolução dona do envio a partir de um evento de rastreio já validado e traduzido. */
public interface RastreioService {
    void processarEvento(EventoRastreio evento);
}
