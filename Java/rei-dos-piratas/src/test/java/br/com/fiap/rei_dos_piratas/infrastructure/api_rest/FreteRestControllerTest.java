package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.FreteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.OpcaoFreteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(FreteRestController.class)
class FreteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FreteController freteController;

    @Test
    void calcularFrete_DeveManterOContratoJsonDoApp() throws Exception {
        // Arrange
        when(freteController.calcularFreteProdutos(any())).thenReturn(List.of(new OpcaoFreteOutDto(
                2L, "SEDEX", new BigDecimal("32.50"), new BigDecimal("30.00"), new BigDecimal("2.50"), "R$", 3,
                new OpcaoFreteOutDto.Company(1L, "Correios", "https://example.com/correios.png"))));

        // Act & Assert
        mockMvc.perform(post("/frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cep_destino":"01001000","itens":[{"produto_id":1,"quantidade":2}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("SEDEX"))
                .andExpect(jsonPath("$[0].price").value(32.50))
                .andExpect(jsonPath("$[0].custom_price").value(30.00))
                .andExpect(jsonPath("$[0].discount").value(2.50))
                .andExpect(jsonPath("$[0].currency").value("R$"))
                .andExpect(jsonPath("$[0].delivery_time").value(3))
                .andExpect(jsonPath("$[0].company.id").value(1))
                .andExpect(jsonPath("$[0].company.name").value("Correios"))
                .andExpect(jsonPath("$[0].company.picture").value("https://example.com/correios.png"));
    }
}
