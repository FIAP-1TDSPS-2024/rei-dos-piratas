package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;

import java.util.function.Function;

/**
 * Estorno de pagamento em três passos, para nunca devolver dinheiro sem registro:
 * T1 aplica {@code registrar} no pedido (que cria o estorno no pagamento) e grava; a chamada ao fornecedor
 * roda fora de transação; T2 relê o pedido e grava o resultado sem desfazer o que um evento já concluiu.
 * Falha do fornecedor marca o estorno FALHOU (não lança): o que motivou o estorno já está gravado.
 */
public interface EstornoPagamentoService {
    Pedido estornar(Long pedidoId, Function<Pedido, Estorno> registrar, String descricao);
}
