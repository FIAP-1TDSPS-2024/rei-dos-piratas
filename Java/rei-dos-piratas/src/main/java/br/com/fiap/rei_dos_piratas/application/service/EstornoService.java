package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;

public interface EstornoService {
    void processarEventoEstorno(Long pedidoId, EventoCobrancaDto evento);
}
