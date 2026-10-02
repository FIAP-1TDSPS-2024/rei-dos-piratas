package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaCidadeEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEnderecoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.endereco.JpaEstadoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.frete.JpaTokenEntity;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DatabaseMigrationIntegrationTest {
    @Autowired private EntityManager entityManager;
    @Autowired private Flyway flyway;

    @Test
    void appliesAllProductionMigrations() {
        assertEquals(31, flyway.info().applied().length);
        assertEquals(0, flyway.info().pending().length);
        flyway.validate();
    }

    @Test
    void generatesIdsAfterSeededRecordsAndFiltersInactiveAddresses() {
        var estado = new JpaEstadoEntity(null, "Parana", "PR");
        entityManager.persist(estado);
        var cidade = new JpaCidadeEntity(null, "Curitiba", estado);
        entityManager.persist(cidade);
        var endereco = new JpaEnderecoEntity(null, 123, "80000000", "Rua Teste", "Centro", true, cidade, null);
        entityManager.persist(endereco);
        entityManager.flush();
        assertTrue(estado.getId() > 1);
        assertTrue(cidade.getId() > 42);
        assertTrue(endereco.getId() > 21);
        entityManager.clear();
        assertNotNull(entityManager.find(JpaEnderecoEntity.class, endereco.getId()));
        var managed = entityManager.find(JpaEnderecoEntity.class, endereco.getId());
        managed.setEnderecoAtivo(false);
        entityManager.flush();
        entityManager.clear();
        assertNull(entityManager.find(JpaEnderecoEntity.class, endereco.getId()));
    }

    @Test
    void persistsLongTokensAsText() {
        String value = "token-".repeat(2000);
        var token = new JpaTokenEntity(null, value, value + "refresh", LocalDate.now(), LocalDate.now().plusDays(1));
        entityManager.persist(token);
        entityManager.flush();
        entityManager.clear();
        var loaded = entityManager.find(JpaTokenEntity.class, token.getId());
        assertEquals(value, loaded.getToken());
        assertEquals(value + "refresh", loaded.getRefreshToken());
    }
}
