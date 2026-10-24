package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CartaoClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CartaoRestController.class)
class CartaoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartaoClienteController cartaoController;

    @Test
    void listarCartoes_DeveRetornar200ComOsCartoesEmSnakeCase() throws Exception {
        when(cartaoController.listarCartoesDoClienteAtual()).thenReturn(List.of(
                new CartaoClienteOutDto(5L, "VISA", "1234", LocalDateTime.of(2026, 10, 1, 10, 30))));

        mockMvc.perform(get("/cartoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(5)))
                .andExpect(jsonPath("$[0].bandeira", is("VISA")))
                .andExpect(jsonPath("$[0].ultimos_digitos", is("1234")))
                .andExpect(jsonPath("$[0].data_cadastro").exists());
    }

    @Test
    void listarCartoes_NaoDeveExporOToken() throws Exception {
        when(cartaoController.listarCartoesDoClienteAtual()).thenReturn(List.of(
                new CartaoClienteOutDto(5L, "VISA", "1234", LocalDateTime.of(2026, 10, 1, 10, 30))));

        mockMvc.perform(get("/cartoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].token").doesNotExist())
                .andExpect(content().string(not(containsString("tok_"))));
    }

    @Test
    void listarCartoes_SemCartoesDeveRetornar200ComListaVazia() throws Exception {
        when(cartaoController.listarCartoesDoClienteAtual()).thenReturn(List.of());

        mockMvc.perform(get("/cartoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listarCartoes_ContaQueNaoEhClienteDeveRetornar400() throws Exception {
        when(cartaoController.listarCartoesDoClienteAtual())
                .thenThrow(new RegraDeNegocioException("Apenas clientes possuem cartões salvos"));

        mockMvc.perform(get("/cartoes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Apenas clientes possuem cartões salvos")));
    }
}
