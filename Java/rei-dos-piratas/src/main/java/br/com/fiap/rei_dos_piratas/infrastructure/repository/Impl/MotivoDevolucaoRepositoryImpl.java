package br.com.fiap.rei_dos_piratas.infrastructure.repository.Impl;

import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.MotivoDevolucaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.JpaMotivoDevolucaoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaMotivoDevolucaoEntityRepository;

import java.util.List;

public class MotivoDevolucaoRepositoryImpl implements MotivoDevolucaoRepository {

    private final JpaMotivoDevolucaoEntityRepository repository;

    public MotivoDevolucaoRepositoryImpl(JpaMotivoDevolucaoEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MotivoDevolucao> findAll() {
        return repository.findAllByAtivoTrueOrderByDescricaoAsc()
                .stream()
                .map(JpaMotivoDevolucaoMapper::toEntity)
                .toList();
    }

    @Override
    public MotivoDevolucao findById(Long id) {
        return repository.findById(id)
                .filter(motivo -> Boolean.TRUE.equals(motivo.getAtivo()))
                .map(JpaMotivoDevolucaoMapper::toEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Motivo de devolucao nao encontrado para o id " + id));
    }
}

