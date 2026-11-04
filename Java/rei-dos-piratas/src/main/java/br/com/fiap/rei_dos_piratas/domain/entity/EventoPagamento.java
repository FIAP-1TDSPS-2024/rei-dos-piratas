package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoPagamento;

import java.time.LocalDateTime;

/**
 * Evento de pagamento recebido do fornecedor, já traduzido para o domínio.
 *
 * @param idEvento     id do evento no fornecedor (chave de idempotência)
 * @param tipoOriginal nome do evento no fornecedor, para log e auditoria
 * @param idCobranca   id da cobrança no fornecedor
 * @param motivoRecusa motivo informado pela operadora, quando houver
 */
public record EventoPagamento(
        String idEvento,
        TipoEventoPagamento tipo,
        String tipoOriginal,
        LocalDateTime ocorridoEm,
        String idCobranca,
        Long pedidoId,
        String motivoRecusa
) {
    /** Motivo da recusa para gravar no pagamento; sem motivo da operadora, um texto padrão com o evento. */
    public String motivoRecusaOuPadrao() {
        return motivoRecusa != null ? motivoRecusa : "Pagamento recusado pela operadora (" + tipoOriginal + ")";
    }
}
