package br.com.fiap.rei_dos_piratas.infrastructure.repository;


import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEnderecoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEnderecoEntityRepository extends JpaRepository<JpaEnderecoEntity, Long> {
    JpaEnderecoEntity findFirstByCepAndNumeroAndCliente_Id(String cep, int numero, Long clienteId);
    Page<JpaEnderecoEntity> findAllByEnderecoAtivoIsTrue(Pageable pageable);
    JpaEnderecoEntity findFirstByEmpresa_Id(Long empresaId);
}
