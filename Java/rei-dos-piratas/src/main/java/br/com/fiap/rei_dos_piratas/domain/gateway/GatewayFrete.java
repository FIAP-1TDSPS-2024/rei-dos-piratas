package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Porta para o fornecedor de frete (hoje o Melhor Envio). Tipos de domínio na entrada e na saída;
 * erros do fornecedor chegam como exceções de {@code domain/exceptions} (ex.: ApiExternaException).
 */
public interface GatewayFrete {

    /** Opções de entrega para os itens, do CEP da loja ao CEP do cliente. */
    List<OpcaoFrete> cotar(String cepOrigem, String cepDestino, List<ItemProdutoPedido> itens);

    /** Cria o envio do pedido (ainda não pago) no fornecedor. */
    EnvioCriado criarEnvio(SolicitacaoEnvio solicitacao);

    /** Cria o envio reverso (devolução) a partir do envio original. */
    EnvioCriado criarEnvioReverso(SolicitacaoEnvioReverso solicitacao);

    /** Compra (paga) os envios; a recusa do fornecedor vem como mensagem, não como exceção. */
    ResultadoCompraEnvios comprarEnvios(List<UUID> envios);

    /** Gera as etiquetas dos envios pagos; o resultado é por envio (pode haver sucesso parcial). */
    Map<UUID, ResultadoEtiqueta> gerarEtiquetas(List<UUID> envios);

    /** Link para imprimir as etiquetas geradas. */
    String obterLinkImpressao(List<UUID> envios);
}
