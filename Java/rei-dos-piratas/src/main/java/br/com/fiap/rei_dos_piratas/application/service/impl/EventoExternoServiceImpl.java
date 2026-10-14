package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.EventoExternoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.EventoExternoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class EventoExternoServiceImpl implements EventoExternoService {

    private final EventoExternoRepository repository;

    private final Duration lease;

    public EventoExternoServiceImpl(EventoExternoRepository repository, Duration lease) {
        this.repository = repository;
        this.lease = lease;
    }

    @Override
    @Transactional
    public Optional<EventoExterno> iniciarProcessamento(OrigemEventoEnum origem, String idExterno, String tipoEvento, String payload) {
        EventoExterno evento = repository.findByOrigemAndIdExternoParaAtualizar(origem, idExterno)
                .orElseGet(() -> repository.salvar(new EventoExterno(origem, idExterno, tipoEvento, payload)));

        if (!evento.iniciarProcessamento(LocalDateTime.now(), lease)) {
            log.info("Evento externo duplicado ignorado: origem={}, idExterno={}, status={}, tentativas={}",
                    origem, idExterno, evento.getStatusProcessamento(), evento.getTentativas());
            return Optional.empty();
        }

        return Optional.of(repository.salvar(evento));
    }

    @Override
    @Transactional
    public void concluir(Long id) {
        EventoExterno evento = buscar(id);
        evento.concluir();
        repository.salvar(evento);
    }

    @Override
    @Transactional
    public void falhar(Long id) {
        EventoExterno evento = buscar(id);
        evento.falhar();
        repository.salvar(evento);
    }

    private EventoExterno buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento externo não encontrado para o id " + id));
    }
}
