package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;

import java.util.Optional;

public interface EventoExternoService {

    /**
     * Registra o evento (se inédito) e inicia uma tentativa de processamento.
     *
     * @return o evento em processamento, ou vazio se for duplicado (já processado ou dentro do lease de outra instância)
     */
    Optional<EventoExterno> iniciarProcessamento(OrigemEventoEnum origem, String idExterno, String tipoEvento, String payload);

    void concluir(Long id);

    void falhar(Long id);
}
