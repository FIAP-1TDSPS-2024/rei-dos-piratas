package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaClienteEntityRepository extends JpaRepository<JpaClienteEntity, Long> {

    JpaClienteEntity findFirstByCpf(String cpf);

    JpaClienteEntity findFirstByEmail(String email);

    JpaClienteEntity findFirstByUserName(String username);

    JpaClienteEntity findByUserName(String username);
}
