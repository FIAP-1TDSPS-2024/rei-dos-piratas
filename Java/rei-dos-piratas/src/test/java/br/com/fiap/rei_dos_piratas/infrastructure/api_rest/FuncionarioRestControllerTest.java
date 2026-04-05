package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;
import br.com.fiap.rei_dos_piratas.domain.Enum.PerfilEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FuncionarioRestController.class)
class FuncionarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FuncionarioController funcionarioController;

    private ObjectMapper objectMapper;
    private Perfil perfilPadrao;
    private Funcionario funcionario;
    private FuncionarioOutDto funcionarioOutDto;

    @BeforeEach
    void setUp() {
        // Configurar ObjectMapper com suporte a Java 8 Time
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        perfilPadrao = new Perfil(1L, "FUNCIONARIO", "Perfil de funcionário", null);

        funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000));

        funcionarioOutDto = new FuncionarioOutDto(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000)
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"FUNCIONARIO_READ"})
    void findAll() throws Exception {
        // Arrange
        List<FuncionarioOutDto> funcionarios = new ArrayList<>();
        funcionarios.add(funcionarioOutDto);
        Page<FuncionarioOutDto> funcionarioPage = new Page<>(1, 0, funcionarios);

        when(funcionarioController.listAll(anyInt(), anyInt())).thenReturn(funcionarioPage);

        // Act & Assert
        this.mockMvc.perform(MockMvcRequestBuilders.get("/funcionarios?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageItems[0].id", is(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"FUNCIONARIO_READ"})
    void findById() throws Exception {
        // Arrange
        when(funcionarioController.findById(anyLong())).thenReturn(funcionarioOutDto);

        // Act & Assert
        this.mockMvc.perform(MockMvcRequestBuilders.get("/funcionarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"FUNCIONARIO_WRITE"})
    void create() throws Exception {
        // Arrange
        FuncionarioInDto funcionarioInDto = new FuncionarioInDto(
                "novojonasdasneves",
                "Jonas da Silva Campos Melo",
                "novojonas@gmail.com",
                "SenhaSegura123",
                PerfilEnum.USER,
                BigDecimal.valueOf(1000));

        FuncionarioOutDto funcionarioNovoOutDto = new FuncionarioOutDto(
                2L,
                "novojonasdasneves",
                "Jonas da Silva Campos Melo",
                "novojonas@gmail.com",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000)
        );

        when(funcionarioController.create(any(FuncionarioInDto.class))).thenReturn(funcionarioNovoOutDto);

        // Act & Assert
        String funcionarioJson = objectMapper.writeValueAsString(funcionarioInDto);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/funcionarios")
                        .with(csrf()) // Adicionar CSRF token para testes
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(funcionarioJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"FUNCIONARIO_WRITE"})
    void update() throws Exception {
        // Arrange
        Funcionario funcionarioParaUpdate = new Funcionario(
                "jonasatualizadodasneves",
                1L,
                "Jonas da Silva Campos Melo Atualizado",
                "jonasatualizado@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1200));

        FuncionarioOutDto funcionarioAtualizadoOutDto = new FuncionarioOutDto(
                1L,
                "jonasatualizadodasneves",
                "Jonas da Silva Campos Melo Atualizado",
                "jonasatualizado@gmail.com",
                true,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1200)
        );

        when(funcionarioController.update(any())).thenReturn(funcionarioAtualizadoOutDto);

        // Act & Assert
        String funcionarioJson = objectMapper.writeValueAsString(funcionarioParaUpdate);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/funcionarios")
                        .with(csrf()) // Adicionar CSRF token para testes
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(funcionarioJson))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"FUNCIONARIO_WRITE"})
    void ativarDesativar() throws Exception {
        // Arrange
        FuncionarioOutDto funcionarioDesativadoOutDto = new FuncionarioOutDto(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                false,
                LocalDate.now(),
                perfilPadrao,
                null,
                BigDecimal.valueOf(1000)
        );

        when(funcionarioController.ativarDesativar(anyLong())).thenReturn(funcionarioDesativadoOutDto);

        // Act & Assert
        this.mockMvc.perform(MockMvcRequestBuilders.put("/funcionarios/1")
                        .with(csrf())) // Adicionar CSRF token para testes
                .andExpect(status().isOk());
    }
}

