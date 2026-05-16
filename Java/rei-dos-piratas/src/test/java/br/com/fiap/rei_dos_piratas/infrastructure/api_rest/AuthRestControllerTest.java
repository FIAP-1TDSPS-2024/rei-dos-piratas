package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import br.com.fiap.rei_dos_piratas.infrastructure.security.TokenBlocklistService;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.LoginRequest;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthController authController;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private TokenBlocklistService tokenBlocklistService;

    @Test
    void loginCliente_DeveRetornarToken() throws Exception {
        LoginRequest request = new LoginRequest("joao@example.com", "senha123");

        // Criar ClienteOutDto para o mock
        ClienteOutDto clienteOutDto = new ClienteOutDto(
                1L,
                "joao",
                "João Silva",
                "11111112398",
                "joao@example.com",
                "11987654321",
                true,
                LocalDate.now(),
                LocalDate.of(1990, 1, 1),
                br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum.M,
                null
        );

        AuthResponse response = new AuthResponse("token-abc", clienteOutDto, null, java.util.List.of("CLIENT"));

        when(authController.login(any(LoginRequest.class))).thenReturn(response);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-abc"))
                .andExpect(jsonPath("$.cliente.user_name").value("joao"))
                .andExpect(jsonPath("$.cliente.email").value("joao@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    void cadastroCliente_DeveRetornarClienteCriado() throws Exception {
        ClienteInDto in = new ClienteInDto(
                "jonas",
                "Jonas Silva",
                "joao@example.com",
                "SenhaSegura123",
                java.time.LocalDate.of(1990,1,1),
                SexoEnum.M,
                "52998224725",
                "11987654321"
        );

        // Criar ClienteOutDto para o mock
        ClienteOutDto clienteOutDto = new ClienteOutDto(
                1L,
                "jonas",
                "Jonas",
                "52998224725",
                "joao@example.com",
                "11987654321",
                true,
                LocalDate.now(),
                LocalDate.of(1990, 1, 1),
                SexoEnum.M,
                null
        );

        AuthResponse out = new AuthResponse("token-abc", clienteOutDto, null, java.util.List.of("CLIENT"));

        when(authController.cadastrar(any(ClienteInDto.class))).thenReturn(out);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-abc"))
                .andExpect(jsonPath("$.cliente.id").value(1))
                .andExpect(jsonPath("$.cliente.user_name").value("jonas"))
                .andExpect(jsonPath("$.cliente.email").value("joao@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }
}
