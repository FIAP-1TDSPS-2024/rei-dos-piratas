package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.UploadImagemProdutoOutDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Telas de produto: imagem enviada direto ao S3 por URL pré-assinada; o produto guarda só a chave. */
@WebMvcTest(ProdutoMvcController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "PRODUTO_WRITE")
class ProdutoMvcControllerTest {

    private static final String CHAVE = "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.png";
    private static final String URL_ASSINADA = "https://bucket.s3.amazonaws.com/" + CHAVE + "?X-Amz-Signature=abc";

    @Autowired MockMvc mvc;
    @MockBean ProdutoController controller;

    private ProdutoOutDto produtoComImagem() {
        return new ProdutoOutDto(7L, "One Piece Volume 100", "Descrição do volume 100", "Eiichiro Oda",
                CategoriaEnum.AVENTURA, CHAVE, new BigDecimal("45.90"), new BigDecimal("45.90"), 3,
                new BigDecimal("2"), new BigDecimal("13"), new BigDecimal("19"), new BigDecimal("0.2"), CondicaoEnum.NOVO)
                .comUrlImagem(URL_ASSINADA);
    }

    private CustomUserDetails funcionario() {
        return new CustomUserDetails(1L, "admin", "x", List.of(new SimpleGrantedAuthority("ROLE_PRODUTO_WRITE")),
                TipoConta.FUNCIONARIO, true);
    }

    // ---- Formulário ----

    @Test
    void formularioNovoTemSeletorDeArquivoChaveOcultaEScript() throws Exception {
        String pagina = mvc.perform(get("/web/produtos/novo"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(pagina).contains("type=\"file\"", "accept=\"image/png,image/jpeg,image/webp\"",
                "data-imagem-produto", "/assets/produto-imagem.js");
        assertThat(pagina).containsPattern("<input[^>]*type=\"hidden\"[^>]*name=\"enderecoImagem\"");
        assertThat(pagina).doesNotContain("placeholder=\"http://...\"");
    }

    @Test
    void formularioDeEdicaoMostraAImagemAtualEMantemAChave() throws Exception {
        when(controller.findById(7L)).thenReturn(produtoComImagem());

        String pagina = mvc.perform(get("/web/produtos/7/editar"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(pagina).contains("value=\"" + CHAVE + "\"");
        assertThat(pagina).contains("src=\"" + URL_ASSINADA.replace("&", "&amp;") + "\"");
    }

    // ---- Exibição ----

    @Test
    void listagemExibeAImagemPelaUrlAssinadaENaoPelaChave() throws Exception {
        when(controller.findAll(0, 10)).thenReturn(new Page<>(1, 0, List.of(produtoComImagem())));

        String pagina = mvc.perform(get("/web/produtos"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(pagina).contains("src=\"" + URL_ASSINADA + "\"");
        assertThat(pagina).doesNotContain("src=\"" + CHAVE + "\"");
    }

    @Test
    void detalheExibeAImagemPelaUrlAssinada() throws Exception {
        when(controller.findById(7L)).thenReturn(produtoComImagem());

        String pagina = mvc.perform(get("/web/produtos/7"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(pagina).contains("src=\"" + URL_ASSINADA + "\"");
    }

    // ---- URL de upload para o formulário ----

    @Test
    void urlDeUploadDevolveChaveEUrlEmJson() throws Exception {
        when(controller.gerarUrlPreAssinadaUpload("image/png")).thenReturn(new UploadImagemProdutoOutDto(CHAVE, URL_ASSINADA));

        mvc.perform(post("/web/produtos/imagem/upload-url")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(CHAVE))
                .andExpect(jsonPath("$.url").value(URL_ASSINADA));
    }

    @Test
    void urlDeUploadComFormatoNaoSuportadoResponde400ComAMensagem() throws Exception {
        when(controller.gerarUrlPreAssinadaUpload("image/gif"))
                .thenThrow(new RegraDeNegocioException("Formato de imagem não suportado. Use PNG, JPEG ou WEBP."));

        mvc.perform(post("/web/produtos/imagem/upload-url")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/gif\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Formato de imagem não suportado. Use PNG, JPEG ou WEBP."));
    }

    @Test
    void urlDeUploadComArmazenamentoForaResponde502ComAMensagem() throws Exception {
        when(controller.gerarUrlPreAssinadaUpload("image/png"))
                .thenThrow(new ArmazenamentoArquivoException("Armazenamento de imagens não configurado. Avise o suporte."));

        mvc.perform(post("/web/produtos/imagem/upload-url")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Armazenamento de imagens não configurado. Avise o suporte."));
    }

    // ---- Salvar ----

    @Test
    void salvarComImagemNaoEnviadaVoltaAoFormularioComAMensagemDoDominio() throws Exception {
        when(controller.create(any())).thenThrow(
                new RegraDeNegocioException("A imagem do produto ainda não foi enviada. Selecione a imagem novamente."));

        // Sem os filtros (addFilters = false) o principal do @AuthenticationPrincipal vem do contexto de teste
        CustomUserDetails funcionario = funcionario();
        TestSecurityContextHolder.setAuthentication(
                new UsernamePasswordAuthenticationToken(funcionario, null, funcionario.getAuthorities()));

        mvc.perform(post("/web/produtos/save")
                        .param("nome", "One Piece Volume 100").param("enderecoImagem", CHAVE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produtos/novo"))
                .andExpect(flash().attribute("erroGeral", "A imagem do produto ainda não foi enviada. Selecione a imagem novamente."));
    }
}
