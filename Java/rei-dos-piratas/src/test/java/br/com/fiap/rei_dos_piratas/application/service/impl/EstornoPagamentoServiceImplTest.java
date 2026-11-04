package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.Estorno;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CobrancaSemRespostaException;
import br.com.fiap.rei_dos_piratas.domain.gateway.ExecutorTransacional;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento;
import br.com.fiap.rei_dos_piratas.domain.gateway.ResultadoEstorno;
import br.com.fiap.rei_dos_piratas.domain.gateway.SolicitacaoEstorno;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstornoPagamentoServiceImplTest {

    /** Executa na hora e registra se o trecho está dentro de uma transação. */
    static class ExecutorFalso implements ExecutorTransacional {
        boolean dentroDeTransacao;

        @Override
        public <T> T emTransacao(Supplier<T> operacao) {
            dentroDeTransacao = true;
            try {
                return operacao.get();
            } finally {
                dentroDeTransacao = false;
            }
        }
    }

    private PedidoRepository repository;
    private GatewayPagamento gateway;
    private ExecutorFalso executor;
    private EstornoPagamentoServiceImpl service;
    private Pedido pedido;
    private final AtomicLong proximoId = new AtomicLong(100);

    private final Function<Pedido, Estorno> estornoTotal = p ->
            p.getPagamento().solicitarEstorno(OrigemEstornoEnum.CANCELAMENTO, null, p.getPagamento().getValor());

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        gateway = mock(GatewayPagamento.class);
        executor = new ExecutorFalso();
        service = new EstornoPagamentoServiceImpl(repository, gateway, executor);

        pedido = pedidoPago(TipoPagamentoEnum.PIX);
        when(repository.findById(10L)).thenReturn(pedido);
        // Como o banco: estorno novo ganha id ao ser gravado
        when(repository.update(any(Pedido.class))).thenAnswer(inv -> {
            Pedido gravado = inv.getArgument(0);
            gravado.getPagamento().getEstornos().stream().filter(e -> e.getId() == null)
                    .forEach(e -> e.setId(proximoId.getAndIncrement()));
            return gravado;
        });
        when(gateway.estornar(any())).thenReturn(new ResultadoEstorno(StatusEstornoEnum.CONCLUIDO, null));
    }

    private Pedido pedidoPago(TipoPagamentoEnum tipo) {
        Pedido novo = new Pedido();
        novo.setId(10L);
        novo.setStatus(StatusEnum.PREPARANDO_ENVIO);
        Pagamento pagamento = Pagamento.pendente(tipo);
        pagamento.setValor(new BigDecimal("221.19"));
        pagamento.setStatus(StatusPagamentoEnum.RECEBIDO);
        pagamento.setIdCobranca("pay_1");
        novo.setPagamento(pagamento);
        return novo;
    }

    private Estorno unicoEstorno() {
        return pedido.getPagamento().getEstornos().getFirst();
    }

    @Test
    void deveGravarOEstornoEmTransacaoAntesDeChamarOFornecedorForaDela() {
        // Arrange
        List<String> passos = new ArrayList<>();
        when(repository.update(any(Pedido.class))).thenAnswer(inv -> {
            passos.add("gravou em transação=" + executor.dentroDeTransacao);
            Pedido gravado = inv.getArgument(0);
            gravado.getPagamento().getEstornos().stream().filter(e -> e.getId() == null).forEach(e -> e.setId(1L));
            return gravado;
        });
        when(gateway.estornar(any())).thenAnswer(inv -> {
            passos.add("estornou em transação=" + executor.dentroDeTransacao);
            return new ResultadoEstorno(StatusEstornoEnum.CONCLUIDO, null);
        });

        // Act
        service.estornar(10L, estornoTotal, "Cancelamento do pedido 10");

        // Assert
        assertThat(passos).startsWith("gravou em transação=true", "estornou em transação=false");
    }

    @Test
    void deveSolicitarAoFornecedorOValorDaCobrancaComADescricao() {
        // Act
        service.estornar(10L, estornoTotal, "Cancelamento do pedido 10");

        // Assert
        ArgumentCaptor<SolicitacaoEstorno> captor = ArgumentCaptor.forClass(SolicitacaoEstorno.class);
        verify(gateway).estornar(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new SolicitacaoEstorno("pay_1", TipoPagamentoEnum.PIX,
                new BigDecimal("221.19"), "Cancelamento do pedido 10"));
    }

    @Test
    void estornoConcluidoNaHoraDeveConcluirEMarcarOPagamentoEstornado() {
        // Act
        service.estornar(10L, estornoTotal, "x");

        // Assert
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.CONCLUIDO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.ESTORNADO);
    }

    @Test
    void estornoEmProcessamentoDeveFicarEmAndamento() {
        // Arrange
        when(gateway.estornar(any())).thenReturn(new ResultadoEstorno(StatusEstornoEnum.EM_ANDAMENTO, null));

        // Act
        service.estornar(10L, estornoTotal, "x");

        // Assert
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.EM_ANDAMENTO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void estornoDeBoletoDeveGuardarOLinkParaOCliente() {
        // Arrange
        pedido = pedidoPago(TipoPagamentoEnum.BOLETO);
        when(repository.findById(10L)).thenReturn(pedido);
        when(gateway.estornar(any())).thenReturn(new ResultadoEstorno(StatusEstornoEnum.AGUARDANDO_CLIENTE, "https://asaas/refund/abc"));

        // Act
        service.estornar(10L, estornoTotal, "x");

        // Assert
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.AGUARDANDO_CLIENTE);
        assertThat(unicoEstorno().getUrlSolicitacaoBoleto()).isEqualTo("https://asaas/refund/abc");
    }

    @Test
    void falhaDoFornecedorDeveMarcarFalhouSemLancar() {
        // Arrange: ex.: saldo insuficiente para estornar PIX
        when(gateway.estornar(any())).thenThrow(new ApiExternaException("Saldo insuficiente"));

        // Act & Assert
        assertThatCode(() -> service.estornar(10L, estornoTotal, "x")).doesNotThrowAnyException();
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.FALHOU);
        assertThat(unicoEstorno().getMotivoFalha()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void semRespostaDoFornecedorDeveManterSolicitadoSemRepetir() {
        // Arrange
        when(gateway.estornar(any())).thenThrow(new CobrancaSemRespostaException("sem resposta"));

        // Act & Assert
        assertThatCode(() -> service.estornar(10L, estornoTotal, "x")).doesNotThrowAnyException();
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.SOLICITADO);
        verify(gateway, times(1)).estornar(any());
    }

    @Test
    void eventoQueConcluiuAntesDaSegundaTransacaoNaoDeveSerDesfeito() {
        // Arrange: o webhook PAYMENT_REFUNDED chegou enquanto a resposta voltava
        when(gateway.estornar(any())).thenAnswer(inv -> {
            pedido.getPagamento().concluirEstorno(unicoEstorno());
            return new ResultadoEstorno(StatusEstornoEnum.EM_ANDAMENTO, null);
        });

        // Act
        service.estornar(10L, estornoTotal, "x");

        // Assert
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.CONCLUIDO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.ESTORNADO);
    }

    @Test
    void pagamentoSemCobrancaNoFornecedorDeveFalharSemChamarOFornecedor() {
        // Arrange
        pedido.getPagamento().setIdCobranca(null);

        // Act
        service.estornar(10L, estornoTotal, "x");

        // Assert
        verifyNoInteractions(gateway);
        assertThat(unicoEstorno().getStatus()).isEqualTo(StatusEstornoEnum.FALHOU);
    }
}
