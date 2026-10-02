package br.com.fiap.rei_dos_piratas.domain.repository;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import java.util.Optional;

public interface ContaRepository {
    Optional<Conta> findByEmail(String email);
    Optional<Conta> findByIdentidade(IdentidadeConta identidade);
    boolean emailOcupado(String email, IdentidadeConta propriaConta);
}
