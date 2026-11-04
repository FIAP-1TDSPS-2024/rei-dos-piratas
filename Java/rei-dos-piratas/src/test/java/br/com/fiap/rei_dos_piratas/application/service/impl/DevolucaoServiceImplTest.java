package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.gateway.EnvioCriado;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayFrete;
import br.com.fiap.rei_dos_piratas.domain.gateway.ResultadoCompraEnvios;
import br.com.fiap.rei_dos_piratas.domain.gateway.SolicitacaoEnvioReverso;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import org.mockito.ArgumentCaptor;
import br.com.fiap.rei_dos_piratas.domain.entity.MotivoDevolucao;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevolucaoServiceImplTest {

    private static final Long SERVICO_DEVOLUCAO_CONFIGURADO = 2L;
    private DevolucaoRepository repository;
    private GatewayFrete gatewayFrete;
    private DevolucaoServiceImpl service;
    private br.com.fiap.rei_dos_piratas.application.service.EstornoPagamentoService estornoPagamentoService;

    @BeforeEach
    void setUp() {
        repository = mock(DevolucaoRepository.class);
        gatewayFrete = mock(GatewayFrete.class);
        estornoPagamentoService = mock(br.com.fiap.rei_dos_piratas.application.service.EstornoPagamentoService.class);
        service = new DevolucaoServiceImpl(repository, gatewayFrete, SERVICO_DEVOLUCAO_CONFIGURADO, estornoPagamentoService);
    }

    private Devolucao prepararDevolucao(List<Long> ids) {
        Devolucao devolucao = new Devolucao();
        devolucao.setId(1L);
        devolucao.setPedidoFrete(UUID.randomUUID());
        devolucao.setStatus(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        when(repository.findByIdsAndStatus(ids, StatusDevolucaoEnum.PREPARANDO_RETORNO))
                .thenReturn(List.of(devolucao));
        return devolucao;
    }

    @Test
    void aprovacaoDevePrepararRetornoAntesDoPagamentoDoFrete() {
        Devolucao devolucao = new Devolucao();
        devolucao.setId(1L);
        devolucao.setPedido(mock(Pedido.class, RETURNS_DEEP_STUBS));
        devolucao.setItens(List.of());
        devolucao.setServicoEntrega(1L);
        UUID freteId = UUID.randomUUID();
        when(repository.findById(1L)).thenReturn(devolucao);
        when(repository.update(devolucao)).thenReturn(devolucao);
        when(gatewayFrete.criarEnvioReverso(any())).thenReturn(new EnvioCriado(freteId, "protocolo", BigDecimal.TEN, null));

        Devolucao aprovada = service.aprovarDevolucao(1L);

        assertThat(aprovada.getStatus()).isEqualTo(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        assertThat(aprovada.getPedidoFrete()).isEqualTo(freteId);
        assertThat(aprovada.getAprovada()).isTrue();
        verify(gatewayFrete, never()).comprarEnvios(anyList());
    }

    @Test
    void findDevolucaoAtivaByPedidoIdDeveIgnorarDevolucoesCanceladasOuRetornadas() {
        Devolucao cancelada = new Devolucao();
        cancelada.setStatus(StatusDevolucaoEnum.CANCELADO);
        Devolucao ativa = new Devolucao();
        ativa.setStatus(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        Devolucao retornada = new Devolucao();
        retornada.setStatus(StatusDevolucaoEnum.RETORNADO);
        when(repository.findAllByPedidoId(42L)).thenReturn(List.of(cancelada, ativa, retornada));

        assertThat(service.findDevolucaoAtivaByPedidoId(42L)).containsSame(ativa);
    }

    @Test
    void findDevolucaoAtivaByPedidoIdDeveRetornarVazioQuandoNaoHouverDevolucaoAtiva() {
        Devolucao cancelada = new Devolucao();
        cancelada.setStatus(StatusDevolucaoEnum.CANCELADO);
        Devolucao retornada = new Devolucao();
        retornada.setStatus(StatusDevolucaoEnum.RETORNADO);
        when(repository.findAllByPedidoId(42L)).thenReturn(List.of(cancelada, retornada));

        assertThat(service.findDevolucaoAtivaByPedidoId(42L)).isEmpty();
    }

    @Test
    void organizarDevePagarSomenteFretesElegiveisEAvancarDiretoParaPostagem() {
        Devolucao devolucao = prepararDevolucao(List.of(1L, 2L));
        List<UUID> fretes = List.of(devolucao.getPedidoFrete());
        when(gatewayFrete.comprarEnvios(fretes))
                .thenReturn(new ResultadoCompraEnvios(null));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L, 2L))).isNull();

        var ordem = inOrder(gatewayFrete, repository);
        ordem.verify(gatewayFrete).comprarEnvios(fretes);
        ordem.verify(repository).updateStatusBatch(List.of(1L), StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);
        verifyNoMoreInteractions(gatewayFrete);
    }

    @Test
    void erroSemMensagemNaoDeveSerInterpretadoComoSucesso() {
        prepararDevolucao(List.of(1L));
        when(gatewayFrete.comprarEnvios(anyList()))
                .thenReturn(new ResultadoCompraEnvios("Saldo insuficiente"));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L))).isEqualTo("Saldo insuficiente");
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void mensagemDoProvedorDeveSerRetornadaSemAvancarStatus() {
        prepararDevolucao(List.of(1L));
        when(gatewayFrete.comprarEnvios(anyList()))
                .thenReturn(new ResultadoCompraEnvios("Frete indisponível"));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L))).isEqualTo("Frete indisponível");
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void falhaExternaNaoDeveAvancarStatus() {
        prepararDevolucao(List.of(1L));
        when(gatewayFrete.comprarEnvios(anyList())).thenThrow(new ApiExternaException("Falha de comunicação"));

        assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(List.of(1L)))
                .isInstanceOf(ApiExternaException.class);
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void nenhumaDevolucaoElegivelNaoDeveChamarFrete() {
        when(repository.findByIdsAndStatus(List.of(1L), StatusDevolucaoEnum.PREPARANDO_RETORNO))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(List.of(1L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(gatewayFrete);
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void devolucaoSemFreteNaoDeveSerPaga() {
        prepararDevolucao(List.of(1L)).setPedidoFrete(null);

        assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(List.of(1L)))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(gatewayFrete);
    }

    @Test
    void listaInvalidaNaoDeveConsultarRepositorioOuFrete() {
        for (List<Long> ids : Arrays.asList(null, List.<Long>of(), List.of(0L), List.of(-1L), Arrays.asList(1L, null))) {
            assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(ids))
                    .isInstanceOf(RegraDeNegocioException.class);
        }
        verifyNoInteractions(repository, gatewayFrete);
    }

    // --- Serviço de entrega da devolução (antes vinha de um @Value na entidade, que nunca era aplicado) ---

    private Devolucao devolucaoAprovavelSemServicoDeEntrega() {
        Devolucao devolucao = new Devolucao();
        devolucao.setId(1L);
        devolucao.setPedido(mock(Pedido.class, RETURNS_DEEP_STUBS));
        devolucao.setItens(List.of());
        when(repository.findById(1L)).thenReturn(devolucao);
        when(repository.update(devolucao)).thenReturn(devolucao);
        when(gatewayFrete.criarEnvioReverso(any())).thenReturn(new EnvioCriado(UUID.randomUUID(), "protocolo", BigDecimal.TEN, null));
        return devolucao;
    }

    private SolicitacaoEnvioReverso requisicaoEnviadaAoFrete() {
        ArgumentCaptor<SolicitacaoEnvioReverso> captor = ArgumentCaptor.forClass(SolicitacaoEnvioReverso.class);
        verify(gatewayFrete).criarEnvioReverso(captor.capture());
        return captor.getValue();
    }

    @Test
    void aprovarDevolucao_SemServicoDeEntregaDeveUsarOServicoConfigurado() {
        devolucaoAprovavelSemServicoDeEntrega();

        service.aprovarDevolucao(1L);

        assertThat(requisicaoEnviadaAoFrete().servico()).isEqualTo(2L);
    }

    @Test
    void aprovarDevolucao_ComServicoDeEntregaDefinidoDeveManterOServicoDaDevolucao() {
        Devolucao devolucao = devolucaoAprovavelSemServicoDeEntrega();
        devolucao.setServicoEntrega(1L);

        service.aprovarDevolucao(1L);

        assertThat(requisicaoEnviadaAoFrete().servico()).isEqualTo(1L);
    }

    @Test
    void aprovarDevolucao_SemServicoNaDevolucaoESemConfiguracaoDeveFalharComMensagemClara() {
        devolucaoAprovavelSemServicoDeEntrega();
        DevolucaoServiceImpl semConfiguracao = new DevolucaoServiceImpl(repository, gatewayFrete, null, estornoPagamentoService);

        assertThatThrownBy(() -> semConfiguracao.aprovarDevolucao(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ME_SERVICE_DEVOLUCAO");
        verify(gatewayFrete, never()).criarEnvioReverso(any());
    }

    @Test
    void solicitarDevolucao_DeveRegistrarOServicoDeEntregaConfigurado() {
        Pedido pedido = mock(Pedido.class);
        when(pedido.getId()).thenReturn(10L);
        MotivoDevolucao motivo = mock(MotivoDevolucao.class);
        when(motivo.getArrependimento()).thenReturn(false);
        Devolucao devolucao = new Devolucao();
        devolucao.setPedido(pedido);
        devolucao.setMotivo(motivo);
        devolucao.setItens(new ArrayList<>());
        when(repository.findAllByPedidoId(10L)).thenReturn(List.of());
        when(repository.create(any(Devolucao.class))).thenAnswer(inv -> inv.getArgument(0));

        Devolucao criada = service.solicitarDevolucao(devolucao);

        assertThat(criada.getServicoEntrega()).isEqualTo(SERVICO_DEVOLUCAO_CONFIGURADO);
    }

    @Test
    void rastreioDevolucaoWebhook_EntregueDeveMarcarRetornadoComADataDoEvento() {
        // Arrange: a conversão de UTC para o fuso da loja é do tradutor (TradutorEventoRastreioTest)
        Devolucao devolucao = new Devolucao();
        devolucao.setId(1L);
        devolucao.setStatus(StatusDevolucaoEnum.EM_RETORNO);
        when(repository.update(any(Devolucao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.rastreioDevolucaoWebhook(devolucao, new br.com.fiap.rei_dos_piratas.domain.entity.EventoRastreio(
                br.com.fiap.rei_dos_piratas.domain.Enum.TipoEventoRastreio.ENTREGUE, "order.delivered", UUID.randomUUID(),
                "delivered", "PROTOCOLO", "TRACK123", "https://example.com/tracking", java.time.LocalDate.of(2026, 10, 23)));

        // Assert
        assertThat(devolucao.getStatus()).isEqualTo(StatusDevolucaoEnum.RETORNADO);
        assertThat(devolucao.getDataConclusao()).isEqualTo(java.time.LocalDate.of(2026, 10, 23));
    }

    // ---- Conclusão da devolução: estorno do valor dos itens (e do frete de ida no arrependimento total) ----

    private Devolucao devolucaoRetornada(br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum status) {
        br.com.fiap.rei_dos_piratas.domain.entity.Produto produto = new br.com.fiap.rei_dos_piratas.domain.entity.Produto();
        produto.setPreco(new BigDecimal("45.90"));
        br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido comprado =
                new br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido(1L, produto, 2, new BigDecimal("45.90"));
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setProdutosAdicionados(List.of(comprado));
        pedido.setValorFrete(new BigDecimal("21.19"));
        MotivoDevolucao motivo = new MotivoDevolucao();
        motivo.setArrependimento(false);
        Devolucao devolucao = new Devolucao();
        devolucao.setId(7L);
        devolucao.setPedido(pedido);
        devolucao.setMotivo(motivo);
        devolucao.setStatus(status);
        devolucao.setItens(List.of(new br.com.fiap.rei_dos_piratas.domain.entity.ItemDevolucao(null, comprado, 1)));
        when(repository.findById(7L)).thenReturn(devolucao);
        return devolucao;
    }

    @Test
    void concluirDevolucao_RetornadaDeveEstornarOValorDosItensNoPagamentoDoPedido() {
        // Arrange
        devolucaoRetornada(StatusDevolucaoEnum.RETORNADO);
        br.com.fiap.rei_dos_piratas.domain.entity.Pagamento pagamento =
                br.com.fiap.rei_dos_piratas.domain.entity.Pagamento.pendente(br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum.PIX);
        pagamento.setValor(new BigDecimal("113.00"));
        pagamento.setStatus(br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum.RECEBIDO);
        Pedido pedidoDoPagamento = new Pedido();
        pedidoDoPagamento.setPagamento(pagamento);
        when(estornoPagamentoService.estornar(eq(10L), any(), any())).thenAnswer(inv -> {
            java.util.function.Function<Pedido, br.com.fiap.rei_dos_piratas.domain.entity.Estorno> registrar = inv.getArgument(1);
            registrar.apply(pedidoDoPagamento);
            return pedidoDoPagamento;
        });

        // Act
        service.concluirDevolucao(7L);

        // Assert
        verify(estornoPagamentoService).estornar(eq(10L), any(), eq("Devolução 7 do pedido 10"));
        assertThat(pagamento.getEstornos()).singleElement().satisfies(estorno -> {
            assertThat(estorno.getOrigem()).isEqualTo(br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum.DEVOLUCAO);
            assertThat(estorno.getDevolucaoId()).isEqualTo(7L);
            assertThat(estorno.getValor()).isEqualByComparingTo("45.90");
        });
    }

    @Test
    void concluirDevolucao_QueAindaNaoRetornouDeveSerRecusadaSemEstornar() {
        // Arrange: o funcionário só conclui depois de receber e conferir o produto
        devolucaoRetornada(StatusDevolucaoEnum.EM_RETORNO);

        // Act & Assert
        assertThatThrownBy(() -> service.concluirDevolucao(7L))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException.class);
        verifyNoInteractions(estornoPagamentoService);
    }
}
