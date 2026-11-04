package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;

/**
 * Resposta do fornecedor ao pedido de estorno: CONCLUIDO, EM_ANDAMENTO (o evento de pagamento confirma)
 * ou AGUARDANDO_CLIENTE (boleto: o cliente informa os dados bancários em {@code urlSolicitacaoBoleto}).
 */
public record ResultadoEstorno(StatusEstornoEnum status, String urlSolicitacaoBoleto) {}
