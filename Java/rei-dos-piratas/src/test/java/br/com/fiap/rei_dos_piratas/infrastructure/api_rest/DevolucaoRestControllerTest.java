package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.infrastructure.config.security.SecurityConfig;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.DevolucaoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevolucaoRestController.class)
@Import(SecurityConfig.class)
class DevolucaoRestControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private DevolucaoController controller;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AutenticacaoService autenticacaoService;
    @MockBean private UsuarioDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "PEDIDO_WRITE")
    void organizarRetorna200SemCorpo() throws Exception {
        mockMvc.perform(put("/devolucoes/organizar-devolucoes")
                        .contentType(MediaType.APPLICATION_JSON).content("[1,2]"))
                .andExpect(status().isOk()).andExpect(content().string(""));
        verify(controller).organizarDevolucoesParaEnvio(List.of(1L, 2L));
    }

    @Test
    @WithMockUser(roles = "PEDIDO_WRITE")
    void organizarRetorna400ComMensagemDeErro() throws Exception {
        when(controller.organizarDevolucoesParaEnvio(List.of(1L))).thenReturn("Saldo insuficiente");
        mockMvc.perform(put("/devolucoes/organizar-devolucoes")
                        .contentType(MediaType.APPLICATION_JSON).content("[1]"))
                .andExpect(status().isBadRequest()).andExpect(content().string("Saldo insuficiente"));
    }

    @Test
    @WithMockUser(roles = "PEDIDO_READ")
    void clienteNaoPodeOrganizarFretes() throws Exception {
        mockMvc.perform(put("/devolucoes/organizar-devolucoes")
                        .contentType(MediaType.APPLICATION_JSON).content("[1]"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(controller);
    }

    @Test
    void anonimoNaoPodeOrganizarFretes() throws Exception {
        mockMvc.perform(put("/devolucoes/organizar-devolucoes")
                        .contentType(MediaType.APPLICATION_JSON).content("[1]"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(controller);
    }
}
