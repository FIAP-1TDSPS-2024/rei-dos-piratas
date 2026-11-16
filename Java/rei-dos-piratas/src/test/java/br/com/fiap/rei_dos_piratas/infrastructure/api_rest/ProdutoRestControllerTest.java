package br.com.fiap.rei_dos_piratas.infrastructure.api_rest;

import static org.mockito.Mockito.verify;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UrlPreassinadaOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UploadImagemProdutoOutDto;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProdutoRestController.class)
class ProdutoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoController produtoController;

    @Test
    void findAll() throws Exception {
        ProdutoOutDto produto = new ProdutoOutDto(
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

        List<ProdutoOutDto> produtos = new ArrayList<>();
        produtos.add(produto);

        Page<ProdutoOutDto> produtosPage = new Page<>(1, 0, produtos);

        when(this.produtoController.findAll(0, 10)).thenReturn(produtosPage);

        this.mockMvc.perform(get("/produtos")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_items", hasSize(1)))
                .andExpect(jsonPath("$.page_items[0].id", is(1)))
                .andExpect(jsonPath("$.page_items[0].nome", is("Produto Teste Número 01")))
                .andExpect(jsonPath("$.page_items[0].preco", is(100)))
                .andExpect(jsonPath("$.page_items[0].estoque", is(50)));
    }

    @Test
    void findById() throws Exception {
        ProdutoOutDto produto = new ProdutoOutDto(
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

        when(this.produtoController.findById(1L)).thenReturn(produto);

        this.mockMvc.perform(get("/produtos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Produto Teste Número 01")))
                .andExpect(jsonPath("$.preco", is(100)))
                .andExpect(jsonPath("$.condicao", is("NOVO")));
    }

    @Test
    void gerarUrlPreAssinadaVisualizacao() throws Exception {
        when(produtoController.gerarUrlPreAssinadaVisualizacao("produtos/capas/capa.jpg"))
                .thenReturn(new UrlPreassinadaOutDto("https://exemplo.com/capa.jpg?assinatura=temporaria"));

        this.mockMvc.perform(get("/produtos/imagem/visualizar")
                        .param("key", "produtos/capas/capa.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", is("https://exemplo.com/capa.jpg?assinatura=temporaria")));
    }

    @Test
    void gerarUrlPreAssinadaUpload() throws Exception {
        when(produtoController.gerarUrlPreAssinadaUpload("image/png"))
                .thenReturn(new UploadImagemProdutoOutDto(
                        "produtos/capas/capa.png",
                        "https://exemplo.com/capa.png?assinatura=temporaria"));

        this.mockMvc.perform(post("/produtos/imagem/upload")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key", is("produtos/capas/capa.png")))
                .andExpect(jsonPath("$.url", is("https://exemplo.com/capa.png?assinatura=temporaria")));
    }

    @Test
    void gerarUrlPreAssinadaUpload_SemContentTypeResponde400() throws Exception {
        this.mockMvc.perform(post("/produtos/imagem/upload")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gerarUrlPreAssinadaUpload_FormatoNaoSuportadoResponde400ComAMensagem() throws Exception {
        when(produtoController.gerarUrlPreAssinadaUpload("application/pdf"))
                .thenThrow(new RegraDeNegocioException("Formato de imagem não suportado. Use PNG, JPEG ou WEBP."));

        this.mockMvc.perform(post("/produtos/imagem/upload")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"application/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Formato de imagem não suportado. Use PNG, JPEG ou WEBP.")));
    }

    @Test
    void gerarUrlPreAssinadaUpload_ArmazenamentoIndisponivelResponde502() throws Exception {
        when(produtoController.gerarUrlPreAssinadaUpload("image/png"))
                .thenThrow(new ArmazenamentoArquivoException("Não foi possível acessar o armazenamento de imagens agora."));

        this.mockMvc.perform(post("/produtos/imagem/upload")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message", is("Não foi possível acessar o armazenamento de imagens agora.")));
    }

    @Test
    void gerarUrlPreAssinadaVisualizacao_KeyInvalidaResponde400() throws Exception {
        when(produtoController.gerarUrlPreAssinadaVisualizacao("financeiro/relatorio.pdf"))
                .thenThrow(new RegraDeNegocioException("A imagem do produto é inválida."));

        this.mockMvc.perform(get("/produtos/imagem/visualizar").param("key", "financeiro/relatorio.pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ExpoeAUrlDeExibicaoDaImagem() throws Exception {
        ProdutoOutDto produto = new ProdutoOutDto(1L, "Produto Teste Número 01", "Descrição", "Autor", CategoriaEnum.AVENTURA,
                "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png", BigDecimal.TEN, BigDecimal.TEN, 1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, CondicaoEnum.NOVO)
                .comUrlImagem("https://exemplo.com/img?assinatura=1");
        when(produtoController.findById(1L)).thenReturn(produto);

        this.mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco_imagem", is("produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png")))
                .andExpect(jsonPath("$.url_imagem", is("https://exemplo.com/img?assinatura=1")));
    }

    @Test
    void findById_ProdutoDesativadoVemComAtivoFalse() throws Exception {
        ProdutoOutDto produto = new ProdutoOutDto(1L, "Produto Teste Número 01", "Descrição", "Autor", CategoriaEnum.AVENTURA,
                "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png", BigDecimal.TEN, BigDecimal.TEN, 1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, CondicaoEnum.NOVO)
                .comAtivo(false);
        when(produtoController.findById(1L)).thenReturn(produto);

        this.mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo", is(false)));
    }

    @Test
    void create() throws Exception {
        ProdutoInDto produtoIn = new ProdutoInDto(
                null,
                "Produto Teste Número 01",
                "Descrição do produto teste número 01 com mais detalhes",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://exemplo.com/imagem.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                CondicaoEnum.NOVO,
                1L
        );

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

        when(this.produtoController.create(any(ProdutoInDto.class))).thenReturn(produtoOut);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String produtoJson = mapper.writeValueAsString(produtoIn);

        this.mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Produto Teste Número 01")))
                .andExpect(jsonPath("$.preco", is(100)))
                .andExpect(jsonPath("$.condicao", is("NOVO")));
    }

    @Test
    void update() throws Exception {
        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                CondicaoEnum.NOVO,
                null);

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

        when(this.produtoController.update(any(Produto.class))).thenReturn(produtoOut);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String produtoJson = mapper.writeValueAsString(produto);

        this.mockMvc.perform(put("/produtos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Produto Teste Número 01")))
                .andExpect(jsonPath("$.preco", is(100)));
    }

    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/produtos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void reativarResponde204() throws Exception {
        this.mockMvc.perform(put("/produtos/3/reativar")).andExpect(status().isNoContent());

        verify(produtoController).reativar(3L);
    }

    @Test
    void listarDesativadosDevolveAPagina() throws Exception {
        ProdutoOutDto desativado = new ProdutoOutDto(4L, "Produto Desativado Número 01", "Descrição", "Autor",
                CategoriaEnum.AVENTURA, null, BigDecimal.TEN, BigDecimal.TEN, 1, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, CondicaoEnum.NOVO).comAtivo(false);
        when(produtoController.listarDesativados(0, 10)).thenReturn(new Page<>(1, 0, List.of(desativado)));

        this.mockMvc.perform(get("/produtos/desativados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page_items[0].id", is(4)))
                .andExpect(jsonPath("$.page_items[0].ativo", is(false)));
    }
}
