package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusDevolucaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemDevolucaoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EstornoOutDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Painel de devoluções do funcionário. Permissão da rota no PermissoesIntegrationTest. */
@WebMvcTest(DevolucaoMvcController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"PEDIDO_WRITE"})
class DevolucaoMvcControllerTest {

    @Autowired MockMvc mvc;
    @MockBean DevolucaoController devolucoes;
    @MockBean PedidoController pedidos;

    private DevolucaoOutDto devolucao(StatusDevolucaoEnum status) {
        return new DevolucaoOutDto(4L, 10L, 1L, "ARREPENDIMENTO", "Desisti da compra", "Não gostei",
                List.of(new ItemDevolucaoOutDto(1L, 31L, 7L, "One Piece Vol. 1", 1, new BigDecimal("45.90"))),
                new BigDecimal("45.90"), null, status, LocalDate.now(), null, null, null, 2L, null, null, null, null, null);
    }

    @Test
    void painelDeveListarAsDevolucoesEmAnalisePorPadrao() throws Exception {
        when(devolucoes.findAllByStatus(0, 20, StatusDevolucaoEnum.EM_ANALISE)).thenReturn(new Page<>(1, 0, List.of(devolucao(StatusDevolucaoEnum.EM_ANALISE))));

        mvc.perform(get("/web/devolucoes"))
                .andExpect(status().isOk())
                .andExpect(view().name("devolucoes"))
                .andExpect(content().string(containsString("/web/devolucoes/4")));
    }

    @Test
    void painelDeveFiltrarPeloStatusEscolhido() throws Exception {
        when(devolucoes.findAllByStatus(0, 20, StatusDevolucaoEnum.RETORNADO)).thenReturn(new Page<>(1, 0, List.of()));

        mvc.perform(get("/web/devolucoes/status/RETORNADO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("statusAtual", StatusDevolucaoEnum.RETORNADO));

        verify(devolucoes).findAllByStatus(0, 20, StatusDevolucaoEnum.RETORNADO);
    }

    @Test
    void detalheEmAnaliseDeveOferecerAprovarERecusar() throws Exception {
        when(devolucoes.findById(4L)).thenReturn(devolucao(StatusDevolucaoEnum.EM_ANALISE));

        mvc.perform(get("/web/devolucoes/4"))
                .andExpect(status().isOk())
                .andExpect(view().name("devolucao-detalhes"))
                .andExpect(content().string(containsString("/web/devolucoes/4/aprovar")))
                .andExpect(content().string(containsString("/web/devolucoes/4/recusar")))
                .andExpect(content().string(not(containsString("/web/devolucoes/4/concluir"))));
    }

    @Test
    void detalheRetornadoDeveOferecerConcluirEMostrarOEstornoDaDevolucao() throws Exception {
        // Arrange: só o estorno desta devolução aparece
        when(devolucoes.findById(4L)).thenReturn(devolucao(StatusDevolucaoEnum.RETORNADO));
        when(pedidos.listarEstornos(10L)).thenReturn(List.of(
                new EstornoOutDto(5L, OrigemEstornoEnum.DEVOLUCAO, 4L, new BigDecimal("45.90"), StatusEstornoEnum.FALHOU, null, LocalDateTime.now(), null),
                new EstornoOutDto(6L, OrigemEstornoEnum.DEVOLUCAO, 99L, new BigDecimal("10.00"), StatusEstornoEnum.CONCLUIDO, null, LocalDateTime.now(), null)));

        // Act & Assert
        mvc.perform(get("/web/devolucoes/4"))
                .andExpect(content().string(containsString("/web/devolucoes/4/concluir")))
                .andExpect(content().string(containsString("FALHOU")))
                .andExpect(content().string(not(containsString("CONCLUIDO"))));
    }

    @Test
    void aprovarDeveDelegarEVoltarAoDetalhe() throws Exception {
        mvc.perform(post("/web/devolucoes/4/aprovar"))
                .andExpect(redirectedUrl("/web/devolucoes/4"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(devolucoes).aprovarDevolucao(4L);
    }

    @Test
    void recusarDeveDelegar() throws Exception {
        mvc.perform(post("/web/devolucoes/4/recusar"))
                .andExpect(redirectedUrl("/web/devolucoes/4"));

        verify(devolucoes).recusarDevolucao(4L);
    }

    @Test
    void concluirDeveDispararOEstornoEVoltarAoDetalhe() throws Exception {
        mvc.perform(post("/web/devolucoes/4/concluir"))
                .andExpect(redirectedUrl("/web/devolucoes/4"))
                .andExpect(flash().attribute("successMessage", containsString("estorno")));

        verify(devolucoes).concluirDevolucao(4L);
    }

    @Test
    void concluirAntesDoRetornoDeveMostrarAMensagemDaRegra() throws Exception {
        when(devolucoes.concluirDevolucao(4L)).thenThrow(new WrongStatusException("A devolução só pode ser concluída depois que os itens retornarem à loja."));

        mvc.perform(post("/web/devolucoes/4/concluir"))
                .andExpect(redirectedUrl("/web/devolucoes/4"))
                .andExpect(flash().attribute("errorMessage", "A devolução só pode ser concluída depois que os itens retornarem à loja."));
    }

    @Test
    void organizarDeveComprarOsFretesDasDevolucoesSelecionadas() throws Exception {
        mvc.perform(post("/web/devolucoes/organizar").param("devolucoesSelecionadas", "4", "5"))
                .andExpect(redirectedUrl("/web/devolucoes/status/AGUARDANDO_POSTAGEM_RETORNO"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(devolucoes).organizarDevolucoesParaEnvio(List.of(4L, 5L));
    }

    @Test
    void organizarComRecusaDoFreteDeveMostrarAMensagemDoFornecedor() throws Exception {
        when(devolucoes.organizarDevolucoesParaEnvio(any())).thenReturn("Saldo insuficiente");

        mvc.perform(post("/web/devolucoes/organizar").param("devolucoesSelecionadas", "4"))
                .andExpect(redirectedUrl("/web/devolucoes/status/PREPARANDO_RETORNO"))
                .andExpect(flash().attribute("errorMessage", "Saldo insuficiente"));
    }

    @Test
    void organizarSemSelecaoNaoDeveChamarOServico() throws Exception {
        mvc.perform(post("/web/devolucoes/organizar"))
                .andExpect(redirectedUrl("/web/devolucoes/status/PREPARANDO_RETORNO"))
                .andExpect(flash().attributeExists("errorMessage"));

        verifyNoInteractions(devolucoes);
    }
}
