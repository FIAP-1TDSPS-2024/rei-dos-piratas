package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.negocio.JpaProdutoEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.entity.usuarios.JpaClienteEntity;
import br.com.fiap.rei_dos_piratas.infrastructure.repository.JpaPedidoEntityRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EnumPersistenceIntegrationTest {
    @Autowired EntityManager em;
    @Autowired JpaPedidoEntityRepository pedidos;

    @BeforeEach
    void dados() {
        em.createNativeQuery("""
                INSERT INTO CLIENTES (id, user_name, nome_completo, email, senha, usuario_ativo,
                    data_cadastro, perfil_id, data_nascimento, sexo, cpf, celular)
                VALUES (9500, 'cliente_enum', 'Cliente Enum Teste', 'enum@example.com', 'test', true,
                    CURRENT_DATE, (SELECT id FROM PERFIS WHERE nome = 'CLIENT'), '1990-01-01', 'M', '52998224725', '11987654321')
                """).executeUpdate();
        em.createNativeQuery("""
                INSERT INTO PRODUTOS (id, nome, descricao, autor, categoria, endereco_imagem,
                    preco, preco_original, estoque, condicao, funcionario_id)
                VALUES (9500, 'Produto Enum Teste', 'Teste de enums textuais', 'Autor Teste', 'AVENTURA',
                    'https://example.com/manga.jpg', 29.90, 29.90, 10, 'NOVO', 1)
                """).executeUpdate();
        em.createNativeQuery("""
                INSERT INTO PEDIDOS (id, data_pedido, valor_total, valor_frete, status, cliente_id, endereco_entrega_id)
                VALUES (9500, CURRENT_DATE, 29.90, 10, 'PREPARANDO_ENVIO', 9500, (SELECT MIN(id) FROM ENDERECO))
                """).executeUpdate();
    }

    @Test
    void consultaEAtualizaStatusComParametrosTextuais() {
        assertThat(pedidos.findByIdsAndStatus(List.of(9500L), StatusEnum.PREPARANDO_ENVIO))
                .extracting(p -> p.getId()).containsExactly(9500L);
        assertThat(pedidos.findAllByStatus(StatusEnum.PREPARANDO_ENVIO, PageRequest.of(0, 10)).getContent())
                .extracting(p -> p.getId()).contains(9500L);
        pedidos.updateStatusBatch(List.of(9500L), StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        em.flush();
        em.clear();
        assertThat(em.createNativeQuery("SELECT status FROM PEDIDOS WHERE id = 9500").getSingleResult())
                .isEqualTo("AGUARDANDO_GERACAO_ETIQUETA");
        assertThat(pedidos.findById(9500L).orElseThrow().getStatus()).isEqualTo(StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
        var produto = em.find(JpaProdutoEntity.class, 9500L);
        produto.setCategoria(CategoriaEnum.FANTASIA);
        produto.setCondicao(CondicaoEnum.USADO);
        em.find(JpaClienteEntity.class, 9500L).setSexo(SexoEnum.F);
        em.flush();
        assertThat(em.createNativeQuery("SELECT categoria FROM PRODUTOS WHERE id = 9500").getSingleResult()).isEqualTo("FANTASIA");
        assertThat(em.createNativeQuery("SELECT condicao FROM PRODUTOS WHERE id = 9500").getSingleResult()).isEqualTo("USADO");
        assertThat(em.createNativeQuery("SELECT sexo FROM CLIENTES WHERE id = 9500").getSingleResult()).isEqualTo("F");
    }

    @Test
    void migracaoConverteOrdinaisLegadosEPreservaNomes() {
        em.createNativeQuery("UPDATE PEDIDOS SET status = '2' WHERE id = 9500").executeUpdate();
        em.createNativeQuery("UPDATE PRODUTOS SET categoria = '1', condicao = '0' WHERE id = 9500").executeUpdate();
        em.createNativeQuery("UPDATE CLIENTES SET sexo = '0' WHERE id = 9500").executeUpdate();
        var script = new ClassPathResource("db.migracao/V33__normaliza_enums_textuais.SQL");
        em.unwrap(Session.class).doWork(connection -> {
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
        });
        em.clear();
        assertThat(pedidos.findByIdsAndStatus(List.of(9500L), StatusEnum.PREPARANDO_ENVIO)).hasSize(1);
        var produto = em.find(JpaProdutoEntity.class, 9500L);
        assertThat(produto.getCategoria()).isEqualTo(CategoriaEnum.AVENTURA);
        assertThat(produto.getCondicao()).isEqualTo(CondicaoEnum.NOVO);
        assertThat(em.find(JpaClienteEntity.class, 9500L).getSexo()).isEqualTo(SexoEnum.M);
    }
}
