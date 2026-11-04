package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.DadosCartaoCredito;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;

/**
 * Porta para a API de pagamentos (hoje o Asaas). O domínio e a aplicação só conhecem esta interface;
 * DTOs, endpoints e códigos de erro do fornecedor ficam no adaptador em infrastructure.
 */
public interface GatewayPagamento {

    /** Cadastra o cliente no fornecedor e devolve o id externo. */
    String registrarCliente(Cliente cliente, Endereco endereco);

    /** Tokeniza o cartão (o cartão nunca é persistido). Recusa: {@code PagamentoRecusadoException}. */
    CartaoTokenizado tokenizarCartao(Cliente cliente, Endereco endereco, DadosCartaoCredito cartao, String ipRemoto);

    /** Cria a cobrança. Cartão recusado: {@code PagamentoRecusadoException}. */
    CobrancaCriada criarCobranca(SolicitacaoCobranca solicitacao);

    QrCodePix obterQrCodePix(String idCobranca);

    LinhaDigitavel obterLinhaDigitavel(String idCobranca);

    /** Pede o estorno (total ou parcial) de uma cobrança paga. */
    ResultadoEstorno estornar(SolicitacaoEstorno solicitacao);

    /** Cancela uma cobrança ainda não paga (não devolve valores já pagos). */
    void cancelarCobranca(String idCobranca);
}
