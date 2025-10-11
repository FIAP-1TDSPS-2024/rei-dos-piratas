package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ClienteController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ClienteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClienteRestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
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
    void findById() {
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}