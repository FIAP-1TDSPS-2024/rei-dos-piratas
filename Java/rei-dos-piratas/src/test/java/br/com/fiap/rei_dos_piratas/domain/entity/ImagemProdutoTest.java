package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoImagemEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagemProdutoTest {

    private static final String CHAVE_VALIDA = "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png";

    @Test
    void tiposAceitosViramAExtensaoCerta() {
        assertThat(TipoImagemEnum.doContentType("image/png")).isEqualTo(TipoImagemEnum.PNG);
        assertThat(TipoImagemEnum.doContentType("image/jpeg")).isEqualTo(TipoImagemEnum.JPEG);
        assertThat(TipoImagemEnum.doContentType("IMAGE/WEBP")).isEqualTo(TipoImagemEnum.WEBP);
        assertThat(TipoImagemEnum.JPEG.extensao()).isEqualTo("jpg");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"image/gif", "application/pdf", "text/html", "image/svg+xml"})
    void tipoNaoSuportadoEhRecusado(String contentType) {
        assertThatThrownBy(() -> TipoImagemEnum.doContentType(contentType))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("PNG, JPEG ou WEBP");
    }

    @Test
    void novaChaveFicaNoPrefixoDeProdutosComAExtensaoDoTipo() {
        String chave = ImagemProduto.novaChave(TipoImagemEnum.WEBP);

        assertThat(chave).startsWith("produtos/").endsWith("/imagem.webp");
        assertThat(ImagemProduto.ehChave(chave)).isTrue();
        assertThat(ImagemProduto.novaChave(TipoImagemEnum.WEBP)).isNotEqualTo(chave);
    }

    @Test
    void chaveValidaEhAceita() {
        assertThat(ImagemProduto.ehChave(CHAVE_VALIDA)).isTrue();
        assertThatCode(() -> ImagemProduto.exigirChave(CHAVE_VALIDA)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "https://bucket.s3.amazonaws.com/produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png",
            "outros/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png",
            "produtos/../outros/arquivo.png",
            "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.gif",
            "produtos/qualquer-coisa/imagem.png",
            "  "})
    void valorQueNaoEhChaveDeImagemDeProdutoEhRecusado(String valor) {
        assertThat(ImagemProduto.ehChave(valor)).isFalse();
        assertThatThrownBy(() -> ImagemProduto.exigirChave(valor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("imagem");
    }

    @Test
    void urlCompletaAntigaEhReconhecidaComoLegado() {
        assertThat(ImagemProduto.ehUrlLegada("https://cf.shopee.com.br/file/abc.jpg")).isTrue();
        assertThat(ImagemProduto.ehUrlLegada("http://imagem.com/manga.jpg")).isTrue();
        assertThat(ImagemProduto.ehUrlLegada(CHAVE_VALIDA)).isFalse();
        assertThat(ImagemProduto.ehUrlLegada(null)).isFalse();
        assertThat(ImagemProduto.ehUrlLegada("javascript:alert(1)")).isFalse();
    }
}
