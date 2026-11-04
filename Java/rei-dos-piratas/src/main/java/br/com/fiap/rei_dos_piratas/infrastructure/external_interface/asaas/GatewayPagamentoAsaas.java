package br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.DadosCartaoCredito;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.asaas.dto.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CobrancaSemRespostaException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador da porta {@link GatewayPagamento} para a API do Asaas.
 * Traduz o domínio para o formato do Asaas e de volta; erros HTTP são traduzidos pelo {@code AsaasErrorDecoder}.
 * Nunca logar dados do cartão, token ou IP.
 */
@Slf4j
public class GatewayPagamentoAsaas implements GatewayPagamento {

    private final CobrancaAppClient api;

    public GatewayPagamentoAsaas(CobrancaAppClient api) {
        this.api = api;
    }

    @Override
    public String registrarCliente(Cliente cliente, Endereco endereco) {
        log.info("[ASAAS] Cadastrando cliente ID={}", cliente.getId());
        ClienteCobrancaResponseDto resposta = this.api.criarNovoCliente(new ClienteCobrancaRequestDto(
                cliente.getNomeCompleto(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getCelular(),
                endereco.getLogradouro(),
                String.valueOf(endereco.getNumero()),
                endereco.getBairro(),
                endereco.getCep(),
                cliente.getId().toString(),
                true));
        return resposta.id();
    }

    @Override
    public CartaoTokenizado tokenizarCartao(Cliente cliente, Endereco endereco, DadosCartaoCredito cartao, String ipRemoto) {
        log.info("[ASAAS] Tokenizando cartão do cliente ID={}", cliente.getId());
        TokenizacaoCartaoResponseDto resposta = this.api.tokenizarCartao(new TokenizacaoCartaoRequestDto(
                cliente.getIdCobranca(),
                new CartaoCreditoRequestDto(
                        cartao.titular(),
                        cartao.numero(),
                        cartao.mesValidade(),
                        cartao.anoValidade(),
                        cartao.cvv()),
                new TitularCartaoRequestDto(
                        cliente.getNomeCompleto(),
                        cliente.getEmail(),
                        cliente.getCpf(),
                        endereco.getCep(),
                        String.valueOf(endereco.getNumero()),
                        cliente.getCelular()),
                ipRemoto));
        return new CartaoTokenizado(resposta.creditCardToken(), resposta.creditCardNumber(), resposta.creditCardBrand());
    }

    @Override
    public CobrancaCriada criarCobranca(SolicitacaoCobranca solicitacao) {
        // Asaas: à vista usa "value"; parcelado usa "installmentCount" + "totalValue"
        boolean parcelado = solicitacao.tipo() == TipoPagamentoEnum.CREDIT_CARD
                && solicitacao.parcelas() != null && solicitacao.parcelas() > 1;

        log.info("[ASAAS] Criando cobrança {} do pedido {}", solicitacao.tipo(), solicitacao.referenciaPedido());
        PagamentoCobrancaRequestDto requisicao = new PagamentoCobrancaRequestDto(
                solicitacao.idClienteExterno(),
                solicitacao.tipo(),
                parcelado ? null : solicitacao.valor(),
                solicitacao.vencimento(),
                solicitacao.descricao(),
                solicitacao.referenciaPedido(),
                parcelado ? solicitacao.parcelas() : null,
                solicitacao.tokenCartao(),
                parcelado ? solicitacao.valor() : null,
                solicitacao.ipRemoto(),
                solicitacao.diasCancelamentoRegistro());

        PagamentoCobrancaResponseDto resposta;
        try {
            resposta = this.api.criarNovaCobranca(requisicao);
        } catch (RetryableException e) {
            // Sem resposta: a cobrança pode ter sido criada. Não repetir (cobrança duplicada); o webhook resolve.
            log.error("[ASAAS] Sem resposta ao criar cobrança do pedido {}: {}", solicitacao.referenciaPedido(), e.getMessage());
            throw new CobrancaSemRespostaException("O provedor de pagamento não respondeu. O pagamento será confirmado assim que houver retorno.");
        }

        boolean confirmada = resposta.status() == AsaasPaymentStatus.CONFIRMED
                || resposta.status() == AsaasPaymentStatus.RECEIVED;
        return new CobrancaCriada(resposta.id(), resposta.netValue(), confirmada, resposta.bankSlipUrl());
    }

    @Override
    public QrCodePix obterQrCodePix(String idCobranca) {
        PixQrCodeResponseDto resposta = this.api.obterQrCodePix(idCobranca);
        return new QrCodePix(resposta.success(), resposta.encodedImage(), resposta.payload(),
                resposta.expirationDate(), resposta.description());
    }

    @Override
    public LinhaDigitavel obterLinhaDigitavel(String idCobranca) {
        LinhaDigitavelBoletoResponseDto resposta = this.api.obterLinhaDigitavelBoleto(idCobranca);
        return new LinhaDigitavel(resposta.identificationField(), resposta.barCode());
    }
}
