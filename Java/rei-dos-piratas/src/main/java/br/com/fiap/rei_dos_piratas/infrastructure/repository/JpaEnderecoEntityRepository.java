package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaEnderecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaEnderecoEntityRepository extends JpaRepository<JpaEnderecoEntity, Long> {
    JpaEnderecoEntity findFirstByCidade_Estado_EstadoNome(String estadoNome);
    JpaEnderecoEntity findFirstByCidade_CidadeNome(String cidadeCidadeNome);
    List<JpaEnderecoEntity> findAllByCliente_Id(Long clienteId);
    JpaEnderecoEntity findFirstByEmpresa_Id(Long empresaId);
}
