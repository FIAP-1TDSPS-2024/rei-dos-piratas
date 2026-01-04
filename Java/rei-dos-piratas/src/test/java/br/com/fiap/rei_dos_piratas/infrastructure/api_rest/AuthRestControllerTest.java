package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.AuthResponse;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.EnderecoInDto;
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

    @Test
    void loginCliente_DeveRetornarToken() throws Exception {
        LoginRequest request = new LoginRequest("joao", "senha123");

        AuthResponse response = new AuthResponse("token-abc", "joao", "joao@example.com", java.util.List.of("CLIENT"));

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
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    void cadastroCliente_DeveRetornarClienteCriado() throws Exception {
        EnderecoInDto endereco = new EnderecoInDto(12345, "12345678", "Rua X", "Bairro Y", "Cidade", "Estado", "SP");
        ClienteInDto in = new ClienteInDto("jonas", "Jonas", "joao@example.com", "SenhaSegura123", java.time.LocalDate.of(1990,1,1), br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum.M, endereco, "12345678900");

        // criar ClienteOutDto completo a partir do DTO de entrada para manter consistência
        br.com.fiap.rei_dos_piratas.domain.entity.Cliente clienteEntity = br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper.toEntity(in);
        clienteEntity.setId(1L);
        ClienteOutDto out = br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper.toDto(clienteEntity);

        when(authController.cadastrar(any(ClienteInDto.class))).thenReturn(out);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userName").value("jonas"))
                .andExpect(jsonPath("$.email").value("joao@example.com"));
    }
}
