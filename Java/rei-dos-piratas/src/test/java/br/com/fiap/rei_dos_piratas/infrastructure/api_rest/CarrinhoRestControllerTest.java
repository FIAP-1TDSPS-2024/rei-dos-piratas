package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoOutDto;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CarrinhoRestController.class)
class CarrinhoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarrinhoController carrinhoController;

    @Test
    void adicionarProduto() throws Exception {
        ItemProdutoInDto itemProduto = new ItemProdutoInDto(1L, 2);

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

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.adicionarProduto(eq(1L), any(ItemProdutoInDto.class))).thenReturn(carrinho);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String itemProdutoJson = mapper.writeValueAsString(itemProduto);

        this.mockMvc.perform(put("/carrinho/cliente/{clienteId}/adicionar", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemProdutoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(1)))
                .andExpect(jsonPath("$.produtosAdicionados[0].produto.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados[0].quantidade", is(2)));
    }

    @Test
    void removerProduto() throws Exception {
        ItemProdutoInDto itemProduto = new ItemProdutoInDto(1L, 1);

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
        produtos.add(new ItemProdutoOutDto(produtoOut, 1));

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.removerProduto(eq(1L), any(ItemProdutoInDto.class))).thenReturn(carrinho);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String itemProdutoJson = mapper.writeValueAsString(itemProduto);

        this.mockMvc.perform(put("/carrinho/cliente/{clienteId}/remover", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemProdutoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(1)));
    }

    @Test
    void limparCarrinho() throws Exception {
        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, new ArrayList<>());

        when(this.carrinhoController.limparCarrinho(1L)).thenReturn(carrinho);

        this.mockMvc.perform(put("/carrinho/cliente/{clienteId}/limpar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(0)));
    }

    @Test
    void visualizarCarrinho() throws Exception {
        ProdutoOutDto produtoOut1 = new ProdutoOutDto(
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

        ProdutoOutDto produtoOut2 = new ProdutoOutDto(
                2L,
                "Produto Teste Número 02",
                "Descrição do produto teste número 02",
                "http://exemplo.com/imagem2.jpg",
                50.0f,
                30,
                8.0f,
                4.0f,
                2.0f,
                CondicaoEnum.USADO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut1, 2));
        produtos.add(new ItemProdutoOutDto(produtoOut2, 1));

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.visualizarCarrinho(1L)).thenReturn(carrinho);

        this.mockMvc.perform(get("/carrinho/cliente/{clienteId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(2)))
                .andExpect(jsonPath("$.produtosAdicionados[0].produto.id", is(1)))
                .andExpect(jsonPath("$.produtosAdicionados[1].produto.id", is(2)));
    }

    @Test
    void finalizarCompra() throws Exception {
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

        when(this.carrinhoController.finalizarCompra(1L)).thenReturn(pedido);

        this.mockMvc.perform(put("/carrinho/cliente/{clienteId}/finalizar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valorTotal", is(200.0)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.produtosAdicionados", hasSize(1)));
    }
}
