package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstornoServiceImplTest {

    private PedidoRepository repository;
    private EstornoServiceImpl service;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        service = new EstornoServiceImpl(repository);

        pedido = new Pedido();
        pedido.setId(10L);
        pedido.setStatus(StatusEnum.CANCELADO);
        Pagamento pagamento = Pagamento.pendente(TipoPagamentoEnum.PIX);
        pagamento.setValor(new BigDecimal("221.19"));
        pagamento.setStatus(StatusPagamentoEnum.RECEBIDO);
        pagamento.setIdCobranca("pay_1");
        pedido.setPagamento(pagamento);
        when(repository.findById(10L)).thenReturn(pedido);
        when(repository.update(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Estorno estornoSolicitado(String valor) {
        return pedido.getPagamento().solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, new BigDecimal(valor));
    }

    private EventoPagamento evento(TipoEventoPagamento tipo, String motivo) {
        return new EventoPagamento("evt_1", tipo, "PAYMENT_" + tipo.name(), LocalDateTime.now(), "pay_1", 10L, motivo);
    }

    @Test
    void estornoConcluidoDeveConcluirOEstornoEmAbertoEMarcarOPagamentoEstornado() {
        // Arrange
        Estorno estorno = estornoSolicitado("221.19");

        // Act
        service.processarEventoEstorno(10L, evento(TipoEventoPagamento.ESTORNADA, null));

        // Assert
        assertThat(estorno.getStatus()).isEqualTo(StatusEstornoEnum.CONCLUIDO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.ESTORNADO);
        verify(repository).update(pedido);
    }

    @Test
    void estornoParcialDeveConcluirSoOEstornoMaisAntigoEmAberto() {
        // Arrange
        Estorno primeiro = estornoSolicitado("100.00");
        Estorno segundo = estornoSolicitado("50.00");

        // Act
        service.processarEventoEstorno(10L, evento(TipoEventoPagamento.ESTORNO_PARCIAL, null));

        // Assert
        assertThat(primeiro.getStatus()).isEqualTo(StatusEstornoEnum.CONCLUIDO);
        assertThat(segundo.getStatus()).isEqualTo(StatusEstornoEnum.SOLICITADO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void estornoEmAndamentoDeveMarcarOEstornoComoEmAndamento() {
        // Arrange
        Estorno estorno = estornoSolicitado("221.19");

        // Act
        service.processarEventoEstorno(10L, evento(TipoEventoPagamento.ESTORNO_EM_ANDAMENTO, null));

        // Assert
        assertThat(estorno.getStatus()).isEqualTo(StatusEstornoEnum.EM_ANDAMENTO);
    }

    @Test
    void estornoNegadoDeveMarcarNegadoComOMotivo() {
        // Arrange
        Estorno estorno = estornoSolicitado("221.19");

        // Act
        service.processarEventoEstorno(10L, evento(TipoEventoPagamento.ESTORNO_NEGADO, "Prazo de estorno expirado"));

        // Assert
        assertThat(estorno.getStatus()).isEqualTo(StatusEstornoEnum.NEGADO);
        assertThat(estorno.getMotivoFalha()).isEqualTo("Prazo de estorno expirado");
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void eventoSemEstornoEmAbertoNaoDeveFalharNemAlterarOPedido() {
        // Arrange: ex.: estorno feito direto no painel do Asaas, ou evento fora de ordem
        // (não lançar: a exceção faria o evento ser reprocessado sem fim)

        // Act & Assert
        assertThatCode(() -> service.processarEventoEstorno(10L, evento(TipoEventoPagamento.ESTORNADA, null)))
                .doesNotThrowAnyException();
        verify(repository, never()).update(any());
    }

    @Test
    void contestacaoDeveSerSoRegistradaSemAlterarOPedido() {
        // Arrange
        estornoSolicitado("221.19");

        // Act
        service.processarEventoEstorno(10L, evento(TipoEventoPagamento.OUTRO_ESTORNO, null));

        // Assert
        verify(repository, never()).update(any());
    }
}
