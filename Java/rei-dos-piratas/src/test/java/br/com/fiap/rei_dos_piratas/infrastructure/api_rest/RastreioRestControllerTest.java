package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.exceptions.AssinaturaInvalidaException;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.melhorenvio.WebhookRastreioMelhorEnvio;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RastreioRestController.class)
class RastreioRestControllerTest {

    private static final String CORPO = "{\"event\":\"order.posted\",\"data\":{\"id\":\"123e4567-e89b-12d3-a456-426614174010\"}}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookRastreioMelhorEnvio webhook;

    @Test
    void webhookValidoDeveResponder200() throws Exception {
        mockMvc.perform(post("/rastreio/webhook").header("x-me-signature", "assinatura")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isOk());

        verify(webhook).receber("assinatura", CORPO);
    }

    @Test
    void assinaturaInvalidaDeveResponder401() throws Exception {
        doThrow(new AssinaturaInvalidaException("Assinatura do webhook inválida")).when(webhook).receber(any(), any());

        mockMvc.perform(post("/rastreio/webhook").header("x-me-signature", "errada")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void semAssinaturaDeveChegarAoWebhookEResponder401() throws Exception {
        doThrow(new AssinaturaInvalidaException("Assinatura do webhook inválida")).when(webhook).receber(isNull(), any());

        mockMvc.perform(post("/rastreio/webhook").contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void falhaAoProcessarDeveResponder500() throws Exception {
        doThrow(new IllegalStateException("banco fora")).when(webhook).receber(any(), any());

        mockMvc.perform(post("/rastreio/webhook").header("x-me-signature", "assinatura")
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO))
                .andExpect(status().isInternalServerError());
    }
}
