package br.com.fiap.rei_dos_piratas.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermissoesIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    @Autowired br.com.fiap.rei_dos_piratas.infrastructure.security.UsuarioDetailsService usuarios;
    @Autowired br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil jwt;
    private String token;

    @BeforeEach
    void loginAdminDaMigracao() throws Exception {
        // Mantem a conta e o perfil reais da migracao; troca apenas a senha na transacao do teste.
        em.createNativeQuery("UPDATE FUNCIONARIOS SET senha = :senha WHERE id = 1")
                .setParameter("senha", encoder.encode("SenhaSegura123")).executeUpdate();
        em.clear();
        var resposta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@reidospiratas.com.br\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        token = mapper.readTree(resposta).get("token").asText();
    }

    @Test
    void loginClientePreservaPermissoesEBloqueiaOperacoesAdministrativas() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_roles", "nome_completo":"Cliente Permissoes", "email":"cliente-roles@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        var resposta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cliente-roles@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String clienteToken = mapper.readTree(resposta).get("token").asText();
        em.flush();
        em.clear();
        var usuario = usuarios.loadUserByIdentidade(jwt.extractIdentidade(clienteToken));
        assertThat(usuario.getAuthorities()).extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CARRINHO_MANAGE", "ROLE_ENDERECO_MANAGE", "ROLE_PEDIDO_READ",
                        "ROLE_PEDIDO_CREATE", "ROLE_PEDIDO_PAGAMENTO", "ROLE_PEDIDO_CANCEL");
        mvc.perform(get("/carrinho").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk());
        mvc.perform(get("/enderecos").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/pedidos").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(post("/produtos").header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/funcionarios").header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/pedidos/status/PREPARANDO_ENVIO").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/produtos/novo").cookie(new Cookie("jwt_token", clienteToken)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/pedidos").cookie(new Cookie("jwt_token", clienteToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCriaProdutoEConsultaPedidosNaApi() throws Exception {
        mvc.perform(post("/produtos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"One Piece Volume Teste", "descricao":"Manga para teste de permissoes",
                 "autor":"Eiichiro Oda", "categoria":"AVENTURA", "preco":29.90,
                 "endereco_imagem":"https://example.com/manga.jpg", "preco_original":29.90, "estoque":10, "altura":20, "largura":14, "profundidade":2, "peso":0.3,
                 "condicao":"NOVO", "funcionario_id":1}
                """))
                .andExpect(status().isCreated());
        mvc.perform(get("/pedidos/status/PREPARANDO_ENVIO").header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void adminAcessaFormulariosCriaProdutoEConsultaPedidosNaWeb() throws Exception {
        Cookie cookie = new Cookie("jwt_token", token);
        mvc.perform(get("/web/produtos/novo").cookie(cookie)).andExpect(status().isOk())
                .andExpect(view().name("produto-form"));
        var csrfResponse = mvc.perform(get("/web/auth/csrf").cookie(cookie)).andExpect(status().isOk())
                .andReturn().getResponse();
        var csrfData = mapper.readTree(csrfResponse.getContentAsString());
        mvc.perform(post("/web/produtos/save").cookie(cookie, csrfResponse.getCookie("XSRF-TOKEN"))
                .header(csrfData.get("headerName").asText(), csrfData.get("token").asText())
                .param("nome", "One Piece Volume Web").param("descricao", "Manga para teste de permissoes")
                .param("autor", "Eiichiro Oda").param("categoria", "AVENTURA")
                .param("enderecoImagem", "https://example.com/manga.jpg").param("precoOriginal", "29.90").param("preco", "29.90").param("estoque", "10").param("condicao", "NOVO")
                .param("altura", "20").param("largura", "14").param("profundidade", "2").param("peso", "0.3"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/web/produtos"))
                .andExpect(flash().attributeExists("successMessage"));
        assertThat(((Number) em.createNativeQuery("SELECT COUNT(*) FROM PRODUTOS WHERE nome = 'One Piece Volume Web'")
                .getSingleResult()).intValue()).isEqualTo(1);
        mvc.perform(get("/web/pedidos").cookie(cookie)).andExpect(status().isOk())
                .andExpect(view().name("pedidos"));
    }

    @Test
    void negacaoDeAcessoNoControllerRetorna403EmVezDe500() throws Exception {
        var controller = org.mockito.Mockito.mock(
                br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController.class);
        org.mockito.Mockito.when(controller.create(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Acesso negado"));
        var isolado = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new br.com.fiap.rei_dos_piratas.infrastructure.api_rest.ProdutoRestController(controller))
                .setControllerAdvice(new br.com.fiap.rei_dos_piratas.interfaces.exception.GlobalExceptionHandler()).build();
        isolado.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Acesso negado"));
    }

    @Test
    void alteracaoDePerfilRevogaPermissoesMesmoComTokenJaEmitido() throws Exception {
        em.createNativeQuery("UPDATE FUNCIONARIOS SET perfil_id = (SELECT id FROM PERFIS WHERE nome = 'CLIENT') WHERE id = 1")
                .executeUpdate();
        em.clear();
        mvc.perform(post("/produtos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/produtos/novo").cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/pedidos").cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/pedidos/status/PREPARANDO_ENVIO"))
                .andExpect(status().isUnauthorized());
    }
}



