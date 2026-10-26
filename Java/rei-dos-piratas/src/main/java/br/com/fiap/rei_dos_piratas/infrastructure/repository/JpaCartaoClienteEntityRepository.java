package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCartaoClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCartaoClienteEntityRepository extends JpaRepository<JpaCartaoClienteEntity, Long> {
    List<JpaCartaoClienteEntity> findByCliente_Id(Long id);
    Optional<JpaCartaoClienteEntity> findByCliente_IdAndToken(Long clienteId, String token);
}
