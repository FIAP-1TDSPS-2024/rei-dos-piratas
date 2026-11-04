package br.com.fiap.rei_dos_piratas.domain.gateway;

import java.math.BigDecimal;

/**
 * Cobrança criada no fornecedor. {@code confirmada}: cartão aprovado na própria criação
 * (PIX/boleto e cartão em análise de risco ficam pendentes até o evento de pagamento).
 */
public record CobrancaCriada(String idCobranca, BigDecimal valorLiquido, boolean confirmada, String urlBoleto) {}
