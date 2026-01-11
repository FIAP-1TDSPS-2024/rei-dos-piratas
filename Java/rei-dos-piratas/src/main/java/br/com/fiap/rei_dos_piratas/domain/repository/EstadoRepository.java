package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Estado;

public interface EstadoRepository {
    Estado save (Estado estado);
    Estado findById(Long id);
    Estado findFirstByNome(String nome);
}
