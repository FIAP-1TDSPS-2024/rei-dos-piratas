package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Detalhe do pedido no painel do funcionário: pagamento, estornos e cancelamento. */
@WebMvcTest(PedidoMvcController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"PEDIDO_WRITE"})
class PedidoMvcControllerTest {

    @Autowired MockMvc mvc;
    @MockBean PedidoController pedidos;

    private PedidoOutDto pedido(StatusEnum status) {
        return new PedidoOutDto(10L, LocalDate.now(), null, null, null, new BigDecimal("113.00"), new BigDecimal("21.20"),
                status, List.of(), "Cliente", "Praça da Sé, 45", null, null, null, null, null, null,
                TipoPagamentoEnum.PIX, null, "pay_1", null, StatusPagamentoEnum.RECEBIDO);
    }

    @Test
    void detalheDeveMostrarOStatusDoPagamentoEOsEstornos() throws Exception {
        when(pedidos.findById(10L)).thenReturn(pedido(StatusEnum.CANCELADO));
        when(pedidos.listarEstornos(10L)).thenReturn(List.of(new EstornoOutDto(5L, OrigemEstornoEnum.CANCELAMENTO, null,
                new BigDecimal("113.00"), StatusEstornoEnum.FALHOU, null, LocalDateTime.now(), null)));

        mvc.perform(get("/web/pedidos/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RECEBIDO")))
                .andExpect(content().string(containsString("FALHOU")));
    }

    @Test
    void detalheDePedidoAindaNaoEnviadoDeveOferecerCancelamento() throws Exception {
        when(pedidos.findById(10L)).thenReturn(pedido(StatusEnum.PREPARANDO_ENVIO));

        mvc.perform(get("/web/pedidos/10"))
                .andExpect(content().string(containsString("/web/pedidos/10/cancelar")));
    }

    @Test
    void cancelarDeveDelegarEVoltarAoDetalhe() throws Exception {
        mvc.perform(post("/web/pedidos/10/cancelar"))
                .andExpect(redirectedUrl("/web/pedidos/10"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(pedidos).cancelarPedido(10L);
    }

    @Test
    void cancelarPedidoJaEnviadoDeveMostrarAMensagem() throws Exception {
        when(pedidos.cancelarPedido(10L)).thenThrow(new WrongStatusException("O pedido já foi entregue"));

        mvc.perform(post("/web/pedidos/10/cancelar"))
                .andExpect(redirectedUrl("/web/pedidos/10"))
                .andExpect(flash().attribute("errorMessage", "O pedido já foi entregue"));
    }
}
