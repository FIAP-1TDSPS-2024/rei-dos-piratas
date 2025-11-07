package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ProdutoOutDto;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PedidoRestController.class)
class PedidoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoController pedidoController;

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void findAllByCliente() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = List.of(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                200.0f,
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtos
        );

        Page<PedidoOutDto> pedidosPage = new Page<>(1, 0, List.of(pedido));

        when(this.pedidoController.findAllByCliente(0, 10)).thenReturn(pedidosPage);

        mockMvc.perform(get("/pedidos")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageItems", hasSize(1)))
                .andExpect(jsonPath("$.pageItems[0].id", is(1)))
                .andExpect(jsonPath("$.pageItems[0].valorTotal", is(200.0)))
                .andExpect(jsonPath("$.pageItems[0].status", is("AGUARDANDO_PAGAMENTO")));
    }

    @Test
    void findById() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                200.0f,
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtos
        );

        when(this.pedidoController.findById(1L)).thenReturn(pedido);

        this.mockMvc.perform(get("/pedidos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valorTotal", is(200.0)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void fazerPedido() throws Exception {
        List<ItemProdutoInDto> produtosIn = new ArrayList<>();
        produtosIn.add(new ItemProdutoInDto(1L, 2));

        PedidoInDto pedidoIn = new PedidoInDto(produtosIn);

        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtosOut = new ArrayList<>();
        produtosOut.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedidoOut = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                200.0f,
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtosOut
        );

        when(this.pedidoController.fazerPedido(any(PedidoInDto.class))).thenReturn(pedidoOut);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String pedidoJson = mapper.writeValueAsString(pedidoIn);

        this.mockMvc.perform(post("/pedidos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valorTotal", is(200.0)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")));
    }

    @Test
    void pagarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                200.0f,
                StatusEnum.PREPARANDO_ENVIO,
                produtos
        );

        when(this.pedidoController.pagarPedido(1L)).thenReturn(pedido);

        this.mockMvc.perform(put("/pedidos/pagamento/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PREPARANDO_ENVIO")));
    }

    @Test
    void enviarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                200.0f,
                StatusEnum.EM_TRANSITO,
                produtos
        );

        when(this.pedidoController.enviarPedido(1L)).thenReturn(pedido);

        this.mockMvc.perform(put("/pedidos/envio/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("EM_TRANSITO")));
    }

    @Test
    void entregarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        LocalDate hoje = LocalDate.now();

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                hoje,
                hoje,
                null,
                200.0f,
                StatusEnum.ENTREGUE,
                produtos
        );

        when(this.pedidoController.entregarPedido(1L)).thenReturn(pedido);

        this.mockMvc.perform(put("/pedidos/entrega/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ENTREGUE")));
    }

    @Test
    void cancelarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "http://exemplo.com/imagem.jpg",
                100.0f,
                50,
                10.0f,
                5.0f,
                3.0f,
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        LocalDate hoje = LocalDate.now();

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                hoje,
                null,
                hoje,
                200.0f,
                StatusEnum.CANCELADO,
                produtos
        );

        when(this.pedidoController.cancelarPedido(1L)).thenReturn(pedido);

        this.mockMvc.perform(put("/pedidos/cancelamento/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CANCELADO")));
    }
}
