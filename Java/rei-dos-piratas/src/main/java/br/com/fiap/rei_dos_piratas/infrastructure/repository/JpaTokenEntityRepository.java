package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.frete.JpaTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaTokenEntityRepository extends JpaRepository<JpaTokenEntity, Long> {

    @Query(value = "SELECT * FROM token ORDER BY data_criacao DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    public JpaTokenEntity findLastToken();
}
