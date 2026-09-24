package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;

import java.util.List;

public interface MotivoDevolucaoRepository {

    List<MotivoDevolucao> findAll();

    MotivoDevolucao findById(Long id);
}

