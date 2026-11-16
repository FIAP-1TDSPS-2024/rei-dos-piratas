package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Excluir" produto é desativar: a linha continua no banco, porque carrinhos, pedidos e devoluções
 * apontam para ela (chave estrangeira); o produto só sai da listagem.
 */
@SpringBootTest
@Transactional
class ProdutoDesativacaoIntegrationTest {

    private static final long PRODUTO = 9701L;

    @Autowired ProdutoService produtos;
    @Autowired EntityManager em;

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }

    @BeforeEach
    void produtoReferenciadoPorUmCarrinho() {
        sql("INSERT INTO CARRINHOS (id) VALUES (9701)");
        sql("""
            INSERT INTO PRODUTOS (id,nome,descricao,autor,categoria,endereco_imagem,preco,preco_original,
                estoque,altura,largura,profundidade,peso,condicao,funcionario_id)
            VALUES (9701,'Produto Para Desativar','Produto referenciado por um carrinho','Autor Teste',
                'AVENTURA','https://example.com/manga.jpg',29.90,39.90,10,20,14,2,0.30,'NOVO',1)
            """);
        sql("INSERT INTO CARRINHO_PRODUTO (produto_id, carrinho_id, quantidade, preco_unitario) VALUES (9701, 9701, 1, 29.90)");
        em.flush();
        em.clear();
    }

    @Test
    void produtoNovoNoBancoComecaAtivo() {
        assertThat(produtos.findById(PRODUTO).isAtivo()).isTrue();
    }

    @Test
    void excluirProdutoReferenciadoDesativaSemApagarALinha() {
        produtos.delete(PRODUTO);
        em.flush();
        em.clear();

        Object ativo = em.createNativeQuery("SELECT ativo FROM PRODUTOS WHERE id = 9701").getSingleResult();
        assertThat(ativo).isEqualTo(false);
        Number itensNoCarrinho = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM CARRINHO_PRODUTO WHERE produto_id = 9701").getSingleResult();
        assertThat(itensNoCarrinho.intValue()).isEqualTo(1);
    }

    @Test
    void produtoDesativadoSaiDaListagemMasContinuaAcessivelPeloId() {
        produtos.delete(PRODUTO);
        em.flush();
        em.clear();

        assertThat(produtos.findAll(0, 1000).pageItems()).extracting(Produto::getId).doesNotContain(PRODUTO);
        Produto desativado = produtos.findById(PRODUTO);
        assertThat(desativado.isAtivo()).isFalse();
        assertThat(desativado.getNome()).isEqualTo("Produto Para Desativar");
    }

    @Test
    void reativarDevolveOProdutoAListagemETiraDosDesativados() {
        produtos.delete(PRODUTO);
        em.flush();
        em.clear();
        assertThat(produtos.listarDesativados(0, 1000).pageItems()).extracting(Produto::getId).contains(PRODUTO);

        produtos.reativar(PRODUTO);
        em.flush();
        em.clear();

        assertThat(produtos.findAll(0, 1000).pageItems()).extracting(Produto::getId).contains(PRODUTO);
        assertThat(produtos.listarDesativados(0, 1000).pageItems()).extracting(Produto::getId).doesNotContain(PRODUTO);
    }

    @Test
    void listaDeDesativadosNaoTrazProdutoAtivo() {
        assertThat(produtos.listarDesativados(0, 1000).pageItems()).extracting(Produto::getId).doesNotContain(PRODUTO);
    }
}
