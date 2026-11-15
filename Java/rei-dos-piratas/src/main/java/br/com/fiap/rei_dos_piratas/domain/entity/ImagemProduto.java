package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoImagemEnum;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Regras da imagem do produto guardada no S3. O produto guarda só a chave do objeto
 * ({@code produtos/<uuid>/imagem.<ext>}); a URL de acesso é gerada (pré-assinada) na leitura.
 * Produtos antigos podem ter uma URL completa (legado), exibida como está.
 */
public final class ImagemProduto {

    /** Validade da URL de envio: o navegador sobe o arquivo logo depois de pedir. */
    public static final Duration VALIDADE_UPLOAD = Duration.ofMinutes(5);
    /** Validade da URL de visualização: cobre uma página de catálogo aberta por um tempo. */
    public static final Duration VALIDADE_VISUALIZACAO = Duration.ofHours(2);

    private static final String PREFIXO = "produtos/";
    private static final Pattern PADRAO_CHAVE = Pattern.compile(
            "produtos/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/imagem[.](png|jpg|webp)");

    private ImagemProduto() {
    }

    public static String novaChave(TipoImagemEnum tipo) {
        return PREFIXO + UUID.randomUUID() + "/imagem." + tipo.extensao();
    }

    public static boolean ehChave(String valor) {
        return valor != null && PADRAO_CHAVE.matcher(valor).matches();
    }

    public static boolean ehUrlLegada(String valor) {
        return valor != null && (valor.startsWith("https://") || valor.startsWith("http://"));
    }

    /** Garante que o valor é uma chave de imagem de produto (e não uma URL ou outro objeto do bucket). */
    public static void exigirChave(String valor) {
        if (!ehChave(valor)) {
            throw new RegraDeNegocioException("A imagem do produto é inválida. Envie a imagem novamente pelo formulário.");
        }
    }
}
