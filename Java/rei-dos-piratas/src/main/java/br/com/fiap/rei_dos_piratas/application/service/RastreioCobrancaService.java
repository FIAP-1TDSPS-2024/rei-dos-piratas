package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;

public interface RastreioCobrancaService {
    void processarEvento(EventoCobrancaDto evento, String payload);
}
