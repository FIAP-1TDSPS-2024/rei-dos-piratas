package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.FuncionarioDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.FuncionarioController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.FuncionarioOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(FuncionarioRestController.class)
class FuncionarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FuncionarioController funcionarioController;

    @Test
    void findAll() throws Exception {
        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(1000));

        List<FuncionarioOutDto> funcionarios = new ArrayList<FuncionarioOutDto>();
        funcionarios.add(FuncionarioDtoMapper.toDto(funcionario));

        Page<FuncionarioOutDto> funcionariosPage = new Page<FuncionarioOutDto>(1, 0, funcionarios);

        when(this.funcionarioController.listAll(0,10)).thenReturn(funcionariosPage);

        this.mockMvc.perform(get("/funcionarios")
                        .param("pageNumber", "0")
                        .param("sizeSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageItems", hasSize(1)))
                .andExpect(jsonPath("$.pageItems[0].id", is(1)))
                .andExpect(jsonPath("$.pageItems[0].userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.pageItems[0].nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.pageItems[0].email", is("jonas@gmail.com")));
    }

    @Test
    void findById() throws Exception {
        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(1000));

        FuncionarioOutDto vendedorDto = FuncionarioDtoMapper.toDto(funcionario);

        when(this.funcionarioController.findById(1L)).thenReturn(vendedorDto);

        this.mockMvc.perform(get("/funcionarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }

    @Test
    void create() throws Exception {
        //O que
        FuncionarioInDto vendedor = new FuncionarioInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER,
                BigDecimal.valueOf(1000));

        FuncionarioOutDto funcionarioOutDto = FuncionarioDtoMapper.toDto(FuncionarioDtoMapper.toEntity(vendedor));
        when(this.funcionarioController.create(vendedor)).thenReturn(funcionarioOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // para LocalDate
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String clienteJson = mapper.writeValueAsString(vendedor);


        this.mockMvc.perform(post("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }

    @Test
    void update() throws Exception {
        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(1000));

        FuncionarioOutDto funcionarioOutDto = FuncionarioDtoMapper.toDto(funcionario);
        when(funcionarioController.update(any())).thenReturn(funcionarioOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String funcionarioJson = mapper.writeValueAsString(funcionario);

        mockMvc.perform(put("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(funcionarioJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }


    @Test
    void ativarDesativar() throws Exception {

        //O que
        Funcionario funcionario = new Funcionario(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                false,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(1000));

        FuncionarioOutDto funcionarioOutDto = FuncionarioDtoMapper.toDto(funcionario);
        when(funcionarioController.ativarDesativar(any())).thenReturn(funcionarioOutDto);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/funcionarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.usuarioAtivo", is(false)));
    }
}