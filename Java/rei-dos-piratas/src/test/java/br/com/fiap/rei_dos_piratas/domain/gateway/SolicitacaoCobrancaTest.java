package br.com.fiap.rei_dos_piratas.domain.gateway;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.CartaoCliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.entity.RegraBoleto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolicitacaoCobrancaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 3);
    private final RegraBoleto regraBoleto = new RegraBoleto(3);

    private Pedido pedidoGravado(TipoPagamentoEnum tipo) {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setIdCobranca("cus_000001");
        Pedido pedido = new Pedido(cliente, null, new ArrayList<>(), 1L, tipo);
        pedido.setId(10L);
        pedido.setValorTotal(new BigDecimal("221.19"));
        pedido.getPagamento().setValor(pedido.getValorTotal());
        return pedido;
    }

    private Pedido pedidoDeCartao(int parcelas) {
        Pedido pedido = pedidoGravado(TipoPagamentoEnum.CREDIT_CARD);
        pedido.getPagamento().setParcelas(parcelas);
        pedido.getPagamento().setCartao(new CartaoCliente(50L, null, "tok_abc123", "1234", "VISA", true, LocalDateTime.now()));
        return pedido;
    }

    @Test
    void pixVenceNoDiaSeguinteSemDadosDeCartaoNemDeBoleto() {
        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(pedidoGravado(TipoPagamentoEnum.PIX), regraBoleto, HOJE, null);

        assertThat(solicitacao.idClienteExterno()).isEqualTo("cus_000001");
        assertThat(solicitacao.tipo()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(solicitacao.valor()).isEqualByComparingTo("221.19");
        assertThat(solicitacao.vencimento()).isEqualTo(HOJE.plusDays(1));
        assertThat(solicitacao.descricao()).isEqualTo("cobrança do pedido 10");
        assertThat(solicitacao.referenciaPedido()).isEqualTo("10");
        assertThat(solicitacao.tokenCartao()).isNull();
        assertThat(solicitacao.ipRemoto()).isNull();
        assertThat(solicitacao.diasCancelamentoRegistro()).isNull();
    }

    @Test
    void cartaoAVistaLevaTokenIpEUmaParcela() {
        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(pedidoDeCartao(1), regraBoleto, HOJE, "200.1.2.3");

        assertThat(solicitacao.tipo()).isEqualTo(TipoPagamentoEnum.CREDIT_CARD);
        assertThat(solicitacao.tokenCartao()).isEqualTo("tok_abc123");
        assertThat(solicitacao.ipRemoto()).isEqualTo("200.1.2.3");
        assertThat(solicitacao.parcelas()).isEqualTo(1);
        assertThat(solicitacao.valor()).isEqualByComparingTo("221.19");
        assertThat(solicitacao.diasCancelamentoRegistro()).isNull();
    }

    @Test
    void cartaoParceladoLevaAQuantidadeDeParcelas() {
        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(pedidoDeCartao(3), regraBoleto, HOJE, "200.1.2.3");

        assertThat(solicitacao.parcelas()).isEqualTo(3);
        assertThat(solicitacao.valor()).isEqualByComparingTo("221.19");
    }

    @Test
    void cartaoSemCartaoTokenizadoNaoPodeSerCobrado() {
        Pedido pedido = pedidoGravado(TipoPagamentoEnum.CREDIT_CARD);

        assertThatThrownBy(() -> SolicitacaoCobranca.para(pedido, regraBoleto, HOJE, "200.1.2.3"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void boletoVencePelaRegraESeraCanceladoUmDiaDepois() {
        SolicitacaoCobranca solicitacao = SolicitacaoCobranca.para(pedidoGravado(TipoPagamentoEnum.BOLETO), regraBoleto, HOJE, null);

        assertThat(solicitacao.tipo()).isEqualTo(TipoPagamentoEnum.BOLETO);
        assertThat(solicitacao.vencimento()).isEqualTo(HOJE.plusDays(3));
        assertThat(solicitacao.diasCancelamentoRegistro()).isEqualTo(1);
        assertThat(solicitacao.ipRemoto()).isNull();
        assertThat(solicitacao.tokenCartao()).isNull();
    }
}
