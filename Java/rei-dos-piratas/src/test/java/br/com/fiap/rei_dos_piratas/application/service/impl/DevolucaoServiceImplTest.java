package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Devolucao;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pagamento.CompraFreteResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DevolucaoServiceImplTest {
    private DevolucaoRepository repository;
    private FreteService freteService;
    private DevolucaoServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(DevolucaoRepository.class);
        freteService = mock(FreteService.class);
        service = new DevolucaoServiceImpl(repository, freteService);
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
        when(freteService.criarPedidoDevolucaoFrete(any())).thenReturn(
                new DevolucaoFreteResponseDto(freteId, "protocolo", 1, null, BigDecimal.TEN,
                        null, null, null, null, null, null, 1));

        Devolucao aprovada = service.aprovarDevolucao(1L);

        assertThat(aprovada.getStatus()).isEqualTo(StatusDevolucaoEnum.PREPARANDO_RETORNO);
        assertThat(aprovada.getPedidoFrete()).isEqualTo(freteId);
        assertThat(aprovada.getAprovada()).isTrue();
        verify(freteService, never()).organizarFretes(anyList());
    }

    @Test
    void organizarDevePagarSomenteFretesElegiveisEAvancarDiretoParaPostagem() {
        Devolucao devolucao = prepararDevolucao(List.of(1L, 2L));
        List<String> fretes = List.of(devolucao.getPedidoFrete().toString());
        when(freteService.organizarFretes(fretes))
                .thenReturn(new CompraFreteResponseDto(null, null, null, null, null, null, null));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L, 2L))).isNull();

        var ordem = inOrder(freteService, repository);
        ordem.verify(freteService).organizarFretes(fretes);
        ordem.verify(repository).updateStatusBatch(List.of(1L), StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO);
        verifyNoMoreInteractions(freteService);
    }

    @Test
    void erroSemMensagemNaoDeveSerInterpretadoComoSucesso() {
        prepararDevolucao(List.of(1L));
        when(freteService.organizarFretes(anyList()))
                .thenReturn(new CompraFreteResponseDto(null, null, null, "Saldo insuficiente", null, null, null));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L))).isEqualTo("Saldo insuficiente");
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void mensagemDoProvedorDeveSerRetornadaSemAvancarStatus() {
        prepararDevolucao(List.of(1L));
        when(freteService.organizarFretes(anyList()))
                .thenReturn(new CompraFreteResponseDto(null, null, "Frete indisponível", null, null, null, null));

        assertThat(service.organizarDevolucoesParaEnvio(List.of(1L))).isEqualTo("Frete indisponível");
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void falhaExternaNaoDeveAvancarStatus() {
        prepararDevolucao(List.of(1L));
        when(freteService.organizarFretes(anyList())).thenThrow(new ApiExternaException("Falha de comunicação"));

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
        verifyNoInteractions(freteService);
        verify(repository, never()).updateStatusBatch(anyList(), any());
    }

    @Test
    void devolucaoSemFreteNaoDeveSerPaga() {
        prepararDevolucao(List.of(1L)).setPedidoFrete(null);

        assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(List.of(1L)))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(freteService);
    }

    @Test
    void listaInvalidaNaoDeveConsultarRepositorioOuFrete() {
        for (List<Long> ids : Arrays.asList(null, List.<Long>of(), List.of(0L), List.of(-1L), Arrays.asList(1L, null))) {
            assertThatThrownBy(() -> service.organizarDevolucoesParaEnvio(ids))
                    .isInstanceOf(RegraDeNegocioException.class);
        }
        verifyNoInteractions(repository, freteService);
    }
}
