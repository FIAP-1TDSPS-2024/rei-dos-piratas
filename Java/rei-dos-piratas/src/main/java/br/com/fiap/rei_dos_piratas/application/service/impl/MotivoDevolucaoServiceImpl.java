package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.MotivoDevolucaoService;
import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;
import br.com.fiap.rei_dos_piratas.domain.repository.MotivoDevolucaoRepository;

import java.util.List;

public class MotivoDevolucaoServiceImpl implements MotivoDevolucaoService {

    private final MotivoDevolucaoRepository repository;

    public MotivoDevolucaoServiceImpl(MotivoDevolucaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MotivoDevolucao> findAll() {
        return repository.findAll();
    }

    @Override
    public MotivoDevolucao findById(Long id) {
        return repository.findById(id);
    }
}

