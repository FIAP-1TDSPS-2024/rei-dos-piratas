package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.entity.RegraBoleto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cobrança a criar para um pedido, sem formato de fornecedor. Como cada tipo vira campos
 * da API (ex.: à vista x parcelado) é decisão do adaptador.
 */
public record SolicitacaoCobranca(
        String idClienteExterno,
        TipoPagamentoEnum tipo,
        BigDecimal valor,
        LocalDate vencimento,
        String descricao,
        String referenciaPedido,
        Integer parcelas,
        String tokenCartao,
        String ipRemoto,
        Integer diasCancelamentoRegistro
) {

    /**
     * @param hoje data de hoje no fuso da loja (base do vencimento)
     * @param ipRemoto IP do comprador; usado só no cartão
     */
    public static SolicitacaoCobranca para(Pedido pedido, RegraBoleto regraBoleto, LocalDate hoje, String ipRemoto) {
        Pagamento pagamento = pedido.getPagamento();
        TipoPagamentoEnum tipo = pagamento.getTipo();
        boolean cartao = tipo == TipoPagamentoEnum.CREDIT_CARD;
        boolean boleto = tipo == TipoPagamentoEnum.BOLETO;

        if (cartao && pagamento.getCartao() == null) {
            throw new RegraDeNegocioException("Pagamento com cartão sem cartão tokenizado.");
        }

        return new SolicitacaoCobranca(
                pedido.getCliente().getIdCobranca(),
                tipo,
                pedido.getValorTotal(),
                boleto ? regraBoleto.vencimentoAPartirDe(hoje) : hoje.plusDays(1),
                "cobrança do pedido " + pedido.getId(),
                pedido.getId().toString(),
                pagamento.getParcelas(),
                cartao ? pagamento.getCartao().getToken() : null,
                cartao ? ipRemoto : null,
                boleto ? RegraBoleto.DIAS_CANCELAMENTO_REGISTRO : null);
    }

    @Override
    public String toString() {
        // Sem token do cartão nem IP: evita vazamento em logs
        return "SolicitacaoCobranca[tipo=" + tipo + ", referenciaPedido=" + referenciaPedido + ", parcelas=" + parcelas + "]";
    }
}
