package br.com.fiap.rei_dos_piratas.domain.Enum;

/** Etapa do envio informada pelo fornecedor de frete. */
public enum TipoEventoRastreio {
    CRIADO,
    PAGO,
    ETIQUETA_GERADA,
    /** Recebido em ponto de distribuição (ex.: Pegaki). */
    RECEBIDO_PONTO,
    POSTADO,
    ENTREGUE,
    NAO_ENTREGUE,
    PAUSADO,
    SUSPENSO,
    CANCELADO,
    EXPIRADO,
    OUTRO
}
