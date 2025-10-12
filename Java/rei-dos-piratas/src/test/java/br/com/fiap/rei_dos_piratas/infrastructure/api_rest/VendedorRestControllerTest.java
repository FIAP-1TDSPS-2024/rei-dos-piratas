package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Vendedor;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.VendedorDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.VendedorController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.VendedorOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
@WebMvcTest(VendedorRestController.class)
class VendedorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendedorController vendedorController;

    @Test
    void findAll() throws Exception {
        //O que
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        List<VendedorOutDto> vendedores = new ArrayList<VendedorOutDto>();
        vendedores.add(VendedorDtoMapper.toDto(vendedor));

        Page<VendedorOutDto> vendedoresPage = new Page<VendedorOutDto>(1, 0, vendedores);

        when(this.vendedorController.listAll(0,10)).thenReturn(vendedoresPage);

        this.mockMvc.perform(get("/vendedores")
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
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        VendedorOutDto vendedorDto = VendedorDtoMapper.toDto(vendedor);

        when(this.vendedorController.findById(1L)).thenReturn(vendedorDto);

        this.mockMvc.perform(get("/vendedores/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }

    @Test
    void create() throws Exception {
        //O que
        VendedorInDto vendedor = new VendedorInDto(
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                Role.USER);

        VendedorOutDto vendedorOutDto = VendedorDtoMapper.toDto(VendedorDtoMapper.toEntity(vendedor));
        when(this.vendedorController.create(vendedor)).thenReturn(vendedorOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // para LocalDate
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String clienteJson = mapper.writeValueAsString(vendedor);


        this.mockMvc.perform(post("/vendedores")
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
        Vendedor vendedor = new Vendedor(
                "jonasdasneves",
                1L,
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                Role.USER);

        VendedorOutDto vendedorOutDto = VendedorDtoMapper.toDto(vendedor);
        when(vendedorController.update(any(Vendedor.class))).thenReturn(vendedorOutDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String vendedorJson = mapper.writeValueAsString(vendedor);

        mockMvc.perform(put("/vendedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vendedorJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is("jonasdasneves")))
                .andExpect(jsonPath("$.nomeCompleto", is("Jonas da Silva Campos Melo")))
                .andExpect(jsonPath("$.email", is("jonas@gmail.com")));
    }


    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/vendedores/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}