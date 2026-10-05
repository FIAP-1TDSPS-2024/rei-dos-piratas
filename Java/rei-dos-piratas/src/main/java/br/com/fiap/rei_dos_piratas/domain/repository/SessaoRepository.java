package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Sessao;
import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository {
    Optional<Sessao> findById(UUID id);
    Optional<Sessao> findByIdParaAtualizar(UUID id);
    void salvar(Sessao sessao);
}
