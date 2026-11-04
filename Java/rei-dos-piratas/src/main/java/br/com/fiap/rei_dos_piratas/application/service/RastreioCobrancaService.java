package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;

public interface RastreioCobrancaService {
    void processarEvento(EventoPagamento evento, String payload);
}
