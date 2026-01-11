package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaEnderecoEntityRepository extends JpaRepository<JpaEnderecoEntity, Long> {
    JpaEnderecoEntity findFirstByCepAndNumeroAndCliente_Id(String cep, int numero, Long clienteId);
    List<JpaEnderecoEntity> findAllByCliente_Id(Long clienteId, Pageable pageable);
    JpaEnderecoEntity findFirstByEmpresa_Id(Long empresaId);
}
