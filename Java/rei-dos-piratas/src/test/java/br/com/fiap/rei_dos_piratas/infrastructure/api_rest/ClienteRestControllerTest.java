package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.ClienteOutDto;
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

    @MockBean
    private ClienteController clienteController;

    @Test
    void findAll() throws Exception {
        //O que
        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuario", null),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

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
        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuario", null),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

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
    void update() throws Exception {
        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuario", null),
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                new Carrinho());

        ClienteOutDto clienteOutDto = ClienteDtoMapper.toDto(cliente);
        when(clienteController.update(any(ClienteInDto.class))).thenReturn(clienteOutDto);

        ClienteInDto inDto = new ClienteInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                LocalDate.of(2000, 03, 16),
                SexoEnum.M,
                "52998224725",
                "11991231234"
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String clienteJson = mapper.writeValueAsString(inDto);

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
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/clientes"))
                .andExpect(status().isNoContent());
    }
}
