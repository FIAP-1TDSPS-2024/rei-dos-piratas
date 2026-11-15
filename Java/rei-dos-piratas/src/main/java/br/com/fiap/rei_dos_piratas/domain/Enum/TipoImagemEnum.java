package br.com.fiap.rei_dos_piratas.domain.Enum;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;

/** Formatos aceitos para a imagem do produto. */
public enum TipoImagemEnum {
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpg"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extensao;

    TipoImagemEnum(String contentType, String extensao) {
        this.contentType = contentType;
        this.extensao = extensao;
    }

    public String contentType() {
        return contentType;
    }

    public String extensao() {
        return extensao;
    }

    /** Tipo pelo Content-Type do arquivo; formato não suportado é erro de negócio. */
    public static TipoImagemEnum doContentType(String contentType) {
        if (contentType != null) {
            for (TipoImagemEnum tipo : values()) {
                if (tipo.contentType.equalsIgnoreCase(contentType.trim())) {
                    return tipo;
                }
            }
        }
        throw new RegraDeNegocioException("Formato de imagem não suportado. Use PNG, JPEG ou WEBP.");
    }
}
