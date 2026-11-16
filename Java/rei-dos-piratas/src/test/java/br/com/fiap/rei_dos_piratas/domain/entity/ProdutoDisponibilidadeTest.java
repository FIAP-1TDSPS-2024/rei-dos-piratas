package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Produto "excluído" fica inativo: some das vendas novas, mas continua nos pedidos antigos. */
class ProdutoDisponibilidadeTest {

    private Produto produto() {
        Produto produto = new Produto(7L);
        produto.setNome("One Piece Volume 100");
        return produto;
    }

    @Test
    void produtoNovoNasceAtivo() {
        assertThat(new Produto().isAtivo()).isTrue();
        assertThat(produto().isAtivo()).isTrue();
    }

    @Test
    void desativarDeixaOProdutoInativo() {
        Produto produto = produto();

        produto.desativar();

        assertThat(produto.isAtivo()).isFalse();
    }

    @Test
    void desativarDuasVezesNaoEhErro() {
        Produto produto = produto();
        produto.desativar();

        assertThatCode(produto::desativar).doesNotThrowAnyException();
        assertThat(produto.isAtivo()).isFalse();
    }

    @Test
    void produtoAtivoEstaDisponivelParaVenda() {
        assertThatCode(() -> produto().exigirDisponivel()).doesNotThrowAnyException();
    }

    @Test
    void produtoInativoNaoEstaDisponivelParaVenda() {
        Produto produto = produto();
        produto.desativar();

        assertThatThrownBy(produto::exigirDisponivel)
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("One Piece Volume 100")
                .hasMessageContaining("não está mais disponível");
    }

    @Test
    void reativarDevolveOProdutoAsVendas() {
        Produto produto = produto();
        produto.desativar();

        produto.reativar();

        assertThat(produto.isAtivo()).isTrue();
        assertThatCode(produto::exigirDisponivel).doesNotThrowAnyException();
    }

    @Test
    void reativarProdutoAtivoNaoMudaNada() {
        Produto produto = produto();

        produto.reativar();

        assertThat(produto.isAtivo()).isTrue();
    }
}
