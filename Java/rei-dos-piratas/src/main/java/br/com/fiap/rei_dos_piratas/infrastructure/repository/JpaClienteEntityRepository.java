package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.JpaClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaClienteEntityRepository extends JpaRepository<JpaClienteEntity, Long> {

    JpaClienteEntity findFirstByEndereco_Cidade_CidadeNome(String cidadeNome);

    JpaClienteEntity findFirstByEndereco_Cidade_Estado_EstadoNome(String estadoNome);

    JpaClienteEntity findFirstByCpf(String cpf);

    JpaClienteEntity findFirstByEmail(String email);

    JpaClienteEntity findFirstByUserName(String username);
}
