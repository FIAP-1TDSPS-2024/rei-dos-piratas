package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;

public interface CidadeRepository {
    Cidade save(Cidade cidade);
    Cidade findById(Long id);
    Cidade findFirstByCidadeNomeAndEstadoNome(String nome, String estado);
}
