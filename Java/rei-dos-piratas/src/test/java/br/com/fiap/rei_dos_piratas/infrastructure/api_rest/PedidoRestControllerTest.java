package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Cidade;
import br.com.fiap.rei_dos_piratas.domain.entity.Endereco;
import br.com.fiap.rei_dos_piratas.domain.entity.Estado;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
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

import java.math.BigDecimal;
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

    private Endereco criarEndereco() {
        Estado estado = new Estado(1L, "São Paulo", "SP");
        Cidade cidade = new Cidade(1L, "São Paulo", estado);
        return new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                true,
                cidade,
                "Brasil",
                "BR",
                null);
    }

    private static final Long FRETE_SERVICE_ID = 3L;

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void findAllByCliente() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01 com mais detalhes",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                50,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO);

        List<ItemProdutoOutDto> produtos = List.of(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(20),
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
                .andExpect(jsonPath("$.pageItems[0].valorTotal", is(200)))
                .andExpect(jsonPath("$.pageItems[0].valorFrete", is(20)))
                .andExpect(jsonPath("$.pageItems[0].status", is("AGUARDANDO_PAGAMENTO")));
    }

    @Test
    void findById() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01 com mais detalhes",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                50,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO);

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(20),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtos
        );

        when(this.pedidoController.findById(1L)).thenReturn(pedido);

        this.mockMvc.perform(get("/pedidos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valorTotal", is(200)))
                .andExpect(jsonPath("$.valorFrete", is(20)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void fazerPedido() throws Exception {
        List<ItemProdutoInDto> produtosIn = new ArrayList<>();
        produtosIn.add(new ItemProdutoInDto(1L, 2));

        PedidoInDto pedidoIn = new PedidoInDto(FRETE_SERVICE_ID, 1L, produtosIn);

        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO);

        List<ItemProdutoOutDto> produtosOut = new ArrayList<>();
        produtosOut.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedidoOut = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(21.19),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtosOut
        );

        when(this.pedidoController.fazerPedido(any(PedidoInDto.class))).thenReturn(pedidoOut);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String pedidoJson = mapper.writeValueAsString(pedidoIn);

        this.mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valorTotal", is(200)))
                .andExpect(jsonPath("$.valorFrete", is(21.19)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")));
    }

    @Test
    void pagarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut, 2));

        PedidoOutDto pedido = new PedidoOutDto(
                1L,
                LocalDate.now(),
                null,
                null,
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(20),
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
    void cancelarPedido() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
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
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(20),
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
