package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ClienteRestController.class)
class ClienteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteController clienteController;

    @Test
    void findAll() throws Exception {
        //O que
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                endereco,
                "12345678978");

        List<ClienteOutDto> clientes = new ArrayList<ClienteOutDto>();
        clientes.add(ClienteDtoMapper.toDto(cliente));

        Page<ClienteOutDto> clientePage = new Page<ClienteOutDto>(1, 0, clientes);

        when(this.clienteController.listAll(0,10)).thenReturn(clientePage);

        this.mockMvc.perform(get("/clientes")
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
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                endereco,
                "12345678978");

        ClienteOutDto clienteDto = ClienteDtoMapper.toDto(cliente);

        when(this.clienteController.findById(1L)).thenReturn(clienteDto);

        this.mockMvc.perform(get("/clientes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }

    @Test
    void create() throws Exception {
        //O que
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        ClienteInDto cliente = new ClienteInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                endereco,
                "12345678978");

        ClienteOutDto clienteOutDto = ClienteDtoMapper.toDto(ClienteDtoMapper.toEntity(cliente));
        when(this.clienteController.create(cliente)).thenReturn(clienteOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // para LocalDate
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String clienteJson = mapper.writeValueAsString(cliente);


        this.mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }

    @Test
    void update() throws Exception {
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR"
        );

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978"
        );

        ClienteOutDto clienteOutDto = ClienteDtoMapper.toDto(cliente);
        when(clienteController.update(any(Cliente.class))).thenReturn(clienteOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String clienteJson = mapper.writeValueAsString(cliente);

        mockMvc.perform(put("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }


    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/clientes/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}