package br.com.fiap.rei_dos_piratas.domain.Enum;

public enum StatusEnum {

    //ENVIO
    AGUARDANDO_PAGAMENTO,
    AGUARDANDO_NF,
    PREPARANDO_ENVIO,
    AGUARDANDO_GERACAO_ETIQUETA,
    AGUARDANDO_POSTAGEM,
    EM_TRANSITO,
    ENTREGUE,

    //CANCELAMENTO
    CANCELADO
}
