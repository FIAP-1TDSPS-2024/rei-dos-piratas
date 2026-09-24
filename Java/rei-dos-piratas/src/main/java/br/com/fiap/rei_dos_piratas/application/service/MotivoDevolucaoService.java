package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;

import java.util.List;

public interface MotivoDevolucaoService {

    List<MotivoDevolucao> findAll();

    MotivoDevolucao findById(Long id);
}

