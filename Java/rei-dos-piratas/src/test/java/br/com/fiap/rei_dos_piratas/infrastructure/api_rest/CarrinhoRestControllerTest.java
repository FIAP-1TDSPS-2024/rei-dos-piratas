package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CarrinhoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoCarrinhoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @MockBean
    private CarrinhoController carrinhoController;

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void adicionarProduto() throws Exception {
        ItemProdutoInDto itemProduto = new ItemProdutoInDto(1L, 2);

        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Numero 01",
                "Descricao do produto teste numero 01",
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

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.adicionarProduto(any(ItemProdutoInDto.class))).thenReturn(carrinho);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String itemProdutoJson = mapper.writeValueAsString(itemProduto);

        this.mockMvc.perform(put("/carrinho/adicionar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemProdutoJson)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("joao").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados", hasSize(1)))
                .andExpect(jsonPath("$.produtos_adicionados[0].produto.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados[0].quantidade", is(2)));
    }

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void removerProduto() throws Exception {
        ItemProdutoInDto itemProduto = new ItemProdutoInDto(1L, 1);

        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Numero 01",
                "Descricao do produto teste numero 01",
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
        produtos.add(new ItemProdutoOutDto(produtoOut, 1));

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.removerProduto(any(ItemProdutoInDto.class))).thenReturn(carrinho);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String itemProdutoJson = mapper.writeValueAsString(itemProduto);

        this.mockMvc.perform(put("/carrinho/remover")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemProdutoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados", hasSize(1)));

        this.mockMvc.perform(put("/carrinho/remover")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemProdutoJson)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("joao").roles("CLIENT")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void limparCarrinho() throws Exception {
        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, new ArrayList<>());

        when(this.carrinhoController.limparCarrinho()).thenReturn(carrinho);

        this.mockMvc.perform(put("/carrinho/limpar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados", hasSize(0)));
    }

    @Test
    void visualizarCarrinho() throws Exception {
        ProdutoOutDto produtoOut1 = new ProdutoOutDto(
                1L,
                "Produto Teste Numero 01",
                "Descricao do produto teste numero 01",
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

        ProdutoOutDto produtoOut2 = new ProdutoOutDto(
                2L,
                "Produto Teste Numero 02",
                "Descricao do produto teste numero 02",
                "Eiichiro Oda",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem2.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO
        );

        List<ItemProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(new ItemProdutoOutDto(produtoOut1, 2));
        produtos.add(new ItemProdutoOutDto(produtoOut2, 1));

        CarrinhoOutDto carrinho = new CarrinhoOutDto(1L, produtos);

        when(this.carrinhoController.visualizarCarrinho()).thenReturn(carrinho);

        this.mockMvc.perform(get("/carrinho").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("joao").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados", hasSize(2)))
                .andExpect(jsonPath("$.produtos_adicionados[0].produto.id", is(1)))
                .andExpect(jsonPath("$.produtos_adicionados[1].produto.id", is(2)));
    }

    @Test
    @WithMockUser(username = "jonas", roles = {"CLIENT"})
    void finalizarCompra() throws Exception {
        ProdutoOutDto produtoOut = new ProdutoOutDto(
                1L,
                "Produto Teste Numero 01",
                "Descricao do produto teste numero 01",
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
                BigDecimal.valueOf(21.19),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                produtos,
                null, null, null, null, null, null, null, null
        );

        PedidoCarrinhoInDto pedidoIn = new PedidoCarrinhoInDto(3L, 1L);
        when(this.carrinhoController.finalizarCompra(any(PedidoCarrinhoInDto.class))).thenReturn(pedido);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String pedidoJson = mapper.writeValueAsString(pedidoIn);

        this.mockMvc.perform(put("/carrinho/finalizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("joao").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.valor_total", is(200)))
                .andExpect(jsonPath("$.valor_frete", is(21.19)))
                .andExpect(jsonPath("$.status", is("AGUARDANDO_PAGAMENTO")))
                .andExpect(jsonPath("$.produtos_adicionados", hasSize(1)));
    }
}
