package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEventoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaEventoExternoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaEventoExternoEntityRepository extends JpaRepository<JpaEventoExternoEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from JpaEventoExternoEntity e where e.origem = :origem and e.idExterno = :idExterno")
    Optional<JpaEventoExternoEntity> findByOrigemAndIdExternoParaAtualizar(@Param("origem") OrigemEventoEnum origem,
                                                                          @Param("idExterno") String idExterno);
}
