package br.com.fiap.rei_dos_piratas.infrastructure.repository;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaCarrinhoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCarrinhoEntityRepository extends JpaRepository<JpaCarrinhoEntity, Long> {

    JpaCarrinhoEntity findByCliente_Id(Long id);
}
