package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;

import java.util.Optional;

public interface EventoExternoRepository {

    Optional<EventoExterno> findById(Long id);

    /** Busca o evento bloqueando o registro até o fim da transação atual. */
    Optional<EventoExterno> findByOrigemAndIdExternoParaAtualizar(OrigemEventoEnum origem, String idExterno);

    EventoExterno salvar(EventoExterno evento);
}
