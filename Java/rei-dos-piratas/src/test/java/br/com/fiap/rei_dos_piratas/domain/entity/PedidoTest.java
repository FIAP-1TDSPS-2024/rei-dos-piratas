package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoTest {

    @Test
    void novoPedidoComTipoDePagamentoDeveCriarPagamentoPendente() {
        // Act
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L, TipoPagamentoEnum.PIX);

        // Assert
        Pagamento pagamento = pedido.getPagamento();
        assertThat(pagamento).isNotNull();
        assertThat(pagamento.getTipo()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(pagamento.getParcelas()).isEqualTo(1);
        assertThat(pagamento.getDataCriacao()).isNotNull();
        assertThat(pagamento.getIdCobranca()).isNull();
    }

    @Test
    void tipoDePagamentoDoPedidoVemDoPagamento() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L);
        pedido.setPagamento(new Pagamento(1L, TipoPagamentoEnum.BOLETO, StatusPagamentoEnum.PENDENTE, "pay_1",
                new BigDecimal("50.00"), null, 1, null, null, LocalDateTime.now(), null));

        // Act & Assert
        assertThat(pedido.getTipoPagamento()).isEqualTo(TipoPagamentoEnum.BOLETO);
    }

    @Test
    void pedidoSemPagamentoTemTipoDePagamentoNulo() {
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L);

        assertThat(pedido.getPagamento()).isNull();
        assertThat(pedido.getTipoPagamento()).isNull();
    }
}
