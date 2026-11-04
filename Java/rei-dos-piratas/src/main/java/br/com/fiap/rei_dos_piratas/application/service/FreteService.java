package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;
import br.com.fiap.rei_dos_piratas.domain.gateway.OpcaoFrete;

import java.util.List;

/** Cotação de frete para o app e para o pedido (CEP da loja ao CEP do cliente). */
public interface FreteService {
    List<OpcaoFrete> calcularFreteProdutos(String cepDestino, List<ItemProdutoPedido> itens);
}
