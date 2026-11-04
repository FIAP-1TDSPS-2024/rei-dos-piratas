package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;

import java.math.BigDecimal;

/** Estorno a pedir ao fornecedor. O tipo do pagamento decide o caminho (boleto exige dados bancários do cliente). */
public record SolicitacaoEstorno(String idCobranca, TipoPagamentoEnum tipo, BigDecimal valor, String descricao) {}
