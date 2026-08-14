package br.com.fiap.rei_dos_piratas.domain.Enum;

public enum MotivoDevolucaoEnum {

    ARREPENDIMENTO("Arrependimento da compra"),
    NAO_ERA_O_ESPERADO("Produto não era o que eu esperava"),
    PRODUTO_DANIFICADO("Produto chegou danificado"),
    PRODUTO_DEFEITUOSO("Produto com defeito"),
    PRODUTO_DIFERENTE_DO_ANUNCIO("Produto diferente do anunciado"),
    PRODUTO_ERRADO("Recebi um produto errado"),
    ATRASO_NA_ENTREGA("Atraso na entrega"),
    OUTRO("Outro motivo");

    private final String descricao;

    MotivoDevolucaoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

