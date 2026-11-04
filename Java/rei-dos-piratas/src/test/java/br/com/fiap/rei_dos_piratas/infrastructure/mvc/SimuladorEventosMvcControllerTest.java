package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.application.service.RastreioCobrancaService;
import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.EventoPagamento;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.WebhookRastreioMelhorEnvio;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.DevolucaoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Simulador de eventos (só com APP_SIMULADOR_EVENTOS=true): usa o mesmo caminho dos webhooks reais. */
class SimuladorEventosMvcControllerTest {

    private static final UUID ENVIO = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");

    static PedidoOutDto pedido(UUID envio) {
        return new PedidoOutDto(10L, LocalDate.now(), null, null, null, new BigDecimal("113.00"), new BigDecimal("21.20"),
                StatusEnum.AGUARDANDO_PAGAMENTO, List.of(), "Cliente", "Praça da Sé, 45", null, envio, null, null, null, null,
                TipoPagamentoEnum.PIX, null, "pay_1", null, StatusPagamentoEnum.PENDENTE);
    }

    @Nested
    @WebMvcTest(controllers = SimuladorEventosMvcController.class, properties = "app.simulador-eventos.enabled=true")
    @AutoConfigureMockMvc(addFilters = false)
    @WithMockUser(roles = {"PEDIDO_WRITE"})
    class Ligado {

        @Autowired MockMvc mvc;
        @MockBean PedidoController pedidos;
        @MockBean DevolucaoController devolucoes;
        @MockBean RastreioCobrancaService rastreioCobranca;
        @MockBean WebhookRastreioMelhorEnvio webhookRastreio;
        @MockBean HmacUtil hmac;

        @BeforeEach
        void setUp() {
            when(pedidos.findById(10L)).thenReturn(pedido(ENVIO));
            when(hmac.generateHmac(any())).thenReturn("assinatura");
        }

        @Test
        void pagamentoConfirmadoDeveEntrarPeloMesmoCaminhoDoWebhookDoAsaas() throws Exception {
            // Act
            mvc.perform(post("/web/simulador/pedidos/10/pagamento").param("evento", "PAYMENT_CONFIRMED"))
                    .andExpect(redirectedUrl("/web/pedidos/10"))
                    .andExpect(flash().attributeExists("successMessage"));

            // Assert
            ArgumentCaptor<EventoPagamento> captor = ArgumentCaptor.forClass(EventoPagamento.class);
            verify(rastreioCobranca).processarEvento(captor.capture(), any());
            assertThat(captor.getValue().tipo()).isEqualTo(TipoEventoPagamento.CONFIRMADA);
            assertThat(captor.getValue().pedidoId()).isEqualTo(10L);
            assertThat(captor.getValue().idCobranca()).isEqualTo("pay_1");
            assertThat(captor.getValue().idEvento()).startsWith("sim_");
        }

        @Test
        void eventoDePagamentoForaDaListaNaoDeveSerSimulado() throws Exception {
            mvc.perform(post("/web/simulador/pedidos/10/pagamento").param("evento", "PAYMENT_QUALQUER"))
                    .andExpect(redirectedUrl("/web/pedidos/10"))
                    .andExpect(flash().attributeExists("errorMessage"));

            verifyNoInteractions(rastreioCobranca);
        }

        @Test
        void rastreioDoPedidoDeveEntrarAssinadoPeloWebhookDoMelhorEnvio() throws Exception {
            // Act
            mvc.perform(post("/web/simulador/pedidos/10/rastreio").param("evento", "order.posted"))
                    .andExpect(redirectedUrl("/web/pedidos/10"));

            // Assert
            ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
            verify(webhookRastreio).receber(eq("assinatura"), corpo.capture());
            assertThat(corpo.getValue()).contains("order.posted", ENVIO.toString());
            verify(hmac).generateHmac(corpo.getValue());
        }

        @Test
        void rastreioDePedidoSemEnvioCriadoDeveMostrarErro() throws Exception {
            when(pedidos.findById(10L)).thenReturn(pedido(null));

            mvc.perform(post("/web/simulador/pedidos/10/rastreio").param("evento", "order.posted"))
                    .andExpect(redirectedUrl("/web/pedidos/10"))
                    .andExpect(flash().attributeExists("errorMessage"));

            verifyNoInteractions(webhookRastreio);
        }

        @Test
        void rastreioDaDevolucaoDeveUsarOEnvioReverso() throws Exception {
            // Arrange
            UUID reverso = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");
            when(devolucoes.findById(4L)).thenReturn(new DevolucaoOutDto(4L, 10L, 1L, null, null, null, List.of(), null, null,
                    StatusDevolucaoEnum.AGUARDANDO_POSTAGEM_RETORNO, LocalDate.now(), null, null, true, 2L, reverso, null, null, null, null));

            // Act
            mvc.perform(post("/web/simulador/devolucoes/4/rastreio").param("evento", "order.delivered"))
                    .andExpect(redirectedUrl("/web/devolucoes/4"));

            // Assert
            ArgumentCaptor<String> corpo = ArgumentCaptor.forClass(String.class);
            verify(webhookRastreio).receber(any(), corpo.capture());
            assertThat(corpo.getValue()).contains("order.delivered", reverso.toString(), "delivered_at");
        }
    }

    @Nested
    @WebMvcTest(controllers = SimuladorEventosMvcController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @WithMockUser(roles = {"PEDIDO_WRITE"})
    class Desligado {

        @Autowired MockMvc mvc;
        @MockBean PedidoController pedidos;
        @MockBean DevolucaoController devolucoes;
        @MockBean RastreioCobrancaService rastreioCobranca;
        @MockBean WebhookRastreioMelhorEnvio webhookRastreio;
        @MockBean HmacUtil hmac;

        @Test
        void semAVariavelOSimuladorNaoExiste() throws Exception {
            mvc.perform(post("/web/simulador/pedidos/10/pagamento").param("evento", "PAYMENT_CONFIRMED"))
                    .andExpect(status().isNotFound());

            verifyNoInteractions(rastreioCobranca);
        }
    }
}
