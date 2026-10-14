package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoExterno;
import br.com.fiap.rei_dos_piratas.domain.repository.EventoExternoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaEventoExternoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaEventoExternoEntityRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public class EventoExternoRepositoryImpl implements EventoExternoRepository {

    private final JpaEventoExternoEntityRepository repository;

    public EventoExternoRepositoryImpl(JpaEventoExternoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EventoExterno> findById(Long id) {
        return repository.findById(id).map(JpaEventoExternoMapper::toEntity);
    }

    @Override
    @Transactional
    public Optional<EventoExterno> findByOrigemAndIdExternoParaAtualizar(OrigemEventoEnum origem, String idExterno) {
        return repository.findByOrigemAndIdExternoParaAtualizar(origem, idExterno).map(JpaEventoExternoMapper::toEntity);
    }

    @Override
    @Transactional
    public EventoExterno salvar(EventoExterno evento) {
        return JpaEventoExternoMapper.toEntity(repository.save(JpaEventoExternoMapper.toJpaEntity(evento)));
    }
}
