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
    // O S3 nunca é chamado nos testes: a imagem do produto "já foi enviada" e a URL de upload é fixa
    @org.springframework.boot.test.mock.mockito.MockBean br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo gatewayArquivo;
    private String token;

    @BeforeEach
    void loginAdminDaMigracao() throws Exception {
        org.mockito.Mockito.when(gatewayArquivo.existe(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(gatewayArquivo.gerarUrlPreAssinadaUpload(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new java.net.URL("https://bucket-teste.s3.amazonaws.com/upload?X-Amz-Signature=teste"));
        org.mockito.Mockito.when(gatewayArquivo.gerarUrlPreAssinadaVisualizacao(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(new java.net.URL("https://bucket-teste.s3.amazonaws.com/imagem?X-Amz-Signature=teste"));
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
    void cartoesSalvosSaoConsultadosPeloClienteENaoPorFuncionarios() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_cartoes", "nome_completo":"Cliente Cartoes", "email":"cliente-cartoes@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        var resposta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cliente-cartoes@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String clienteToken = mapper.readTree(resposta).get("token").asText();

        mvc.perform(get("/cartoes").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        // Admin (funcionario) nao tem cartoes salvos: bloqueado pela permissao, nao chega ao servico
        mvc.perform(get("/cartoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/cartoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void areaDoClienteNaWebEhSoDoClienteENaoDoFuncionario() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_web", "nome_completo":"Cliente Web", "email":"cliente-web@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        var resposta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cliente-web@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String clienteToken = mapper.readTree(resposta).get("token").asText();

        mvc.perform(get("/web/conta/carrinho").cookie(new Cookie("jwt_token", clienteToken)))
                .andExpect(status().isOk());
        // Funcionário não tem carrinho nem pedidos próprios: bloqueado pela permissão
        mvc.perform(get("/web/conta/carrinho").cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/conta/carrinho").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
        // Painel de devoluções é do funcionário
        mvc.perform(get("/web/devolucoes").cookie(new Cookie("jwt_token", clienteToken)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/web/devolucoes").cookie(new Cookie("jwt_token", token)))
                .andExpect(status().isOk());
    }

    @Test
    void paginaDeCadastroWebEhPublicaSemAbrirAAreaDoCliente() throws Exception {
        mvc.perform(get("/web/cadastro").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
        mvc.perform(get("/web/conta/pedidos").accept(MediaType.TEXT_HTML)).andExpect(status().is3xxRedirection());
    }

    @Test
    void consultaDeCepDaAreaDoClienteNaoEhAbertaAAnonimoNemAFuncionario() throws Exception {
        mvc.perform(get("/web/conta/cep/01001000").accept(MediaType.TEXT_HTML)).andExpect(status().is3xxRedirection());
        mvc.perform(get("/web/conta/cep/01001000").cookie(new Cookie("jwt_token", token))).andExpect(status().isForbidden());
    }

    @Test
    void urlDeUploadDeImagemNaWebEhSoDeQuemCadastraProduto() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_upload", "nome_completo":"Cliente Upload", "email":"cliente-upload@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"F",
                 "cpf":"11144477735", "celular":"11987654322"}
                """))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        String clienteToken = mapper.readTree(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente-upload@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();

        // Cliente logado, com CSRF válido: o 403 vem da permissão, não do CSRF
        Cookie cliente = new Cookie("jwt_token", clienteToken);
        var csrfCliente = mvc.perform(get("/web/auth/csrf").cookie(cliente)).andReturn().getResponse();
        var dadosCliente = mapper.readTree(csrfCliente.getContentAsString());
        mvc.perform(post("/web/produtos/imagem/upload-url").cookie(cliente, csrfCliente.getCookie("XSRF-TOKEN"))
                        .header(dadosCliente.get("headerName").asText(), dadosCliente.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isForbidden());

        Cookie admin = new Cookie("jwt_token", token);
        var csrfAdmin = mvc.perform(get("/web/auth/csrf").cookie(admin)).andReturn().getResponse();
        var dadosAdmin = mapper.readTree(csrfAdmin.getContentAsString());
        mvc.perform(post("/web/produtos/imagem/upload-url").cookie(admin, csrfAdmin.getCookie("XSRF-TOKEN"))
                        .header(dadosAdmin.get("headerName").asText(), dadosAdmin.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.startsWith("produtos/")));

        // Na API, o mesmo vale para quem não tem PRODUTO_WRITE
        mvc.perform(post("/produtos/imagem/upload").header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void produtosDesativadosEReativacaoSaoSoDeQuemCadastraProduto() throws Exception {
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_reativa", "nome_completo":"Cliente Reativa", "email":"cliente-reativa@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"39053344705", "celular":"11987654323"}
                """))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        String clienteToken = mapper.readTree(mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente-reativa@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();

        // API: a lista de desativados não é pública como o resto de GET /produtos
        mvc.perform(get("/produtos/desativados")).andExpect(status().is4xxClientError());
        mvc.perform(get("/produtos/desativados").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/produtos/desativados").header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(put("/produtos/999999/reativar").header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());

        // Web: lista e POST de reativar só para PRODUTO_WRITE (cliente com CSRF válido leva 403 da permissão)
        Cookie cliente = new Cookie("jwt_token", clienteToken);
        mvc.perform(get("/web/produtos/desativados").cookie(cliente)).andExpect(status().isForbidden());
        var csrfCliente = mvc.perform(get("/web/auth/csrf").cookie(cliente)).andReturn().getResponse();
        var dadosCliente = mapper.readTree(csrfCliente.getContentAsString());
        mvc.perform(post("/web/produtos/999999/reativar").cookie(cliente, csrfCliente.getCookie("XSRF-TOKEN"))
                        .header(dadosCliente.get("headerName").asText(), dadosCliente.get("token").asText()))
                .andExpect(status().isForbidden());

        Cookie admin = new Cookie("jwt_token", token);
        mvc.perform(get("/web/produtos/desativados").cookie(admin)).andExpect(status().isOk())
                .andExpect(view().name("produtos-desativados"));
        var csrfAdmin = mvc.perform(get("/web/auth/csrf").cookie(admin)).andReturn().getResponse();
        var dadosAdmin = mapper.readTree(csrfAdmin.getContentAsString());
        mvc.perform(post("/web/produtos/999999/reativar").cookie(admin, csrfAdmin.getCookie("XSRF-TOKEN"))
                        .header(dadosAdmin.get("headerName").asText(), dadosAdmin.get("token").asText()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produtos/desativados"));
    }

    @Test
    void visualizacaoPublicaDeImagemSoAssinaImagensDeProduto() throws Exception {
        mvc.perform(get("/produtos/imagem/visualizar").param("key", "financeiro/relatorio.pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCriaProdutoEConsultaPedidosNaApi() throws Exception {
        mvc.perform(post("/produtos").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"One Piece Volume Teste", "descricao":"Manga para teste de permissoes",
                 "autor":"Eiichiro Oda", "categoria":"AVENTURA", "preco":29.90,
                 "endereco_imagem":"produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.jpg", "preco_original":29.90, "estoque":10, "altura":20, "largura":14, "profundidade":2, "peso":0.3,
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
                .param("enderecoImagem", "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.jpg").param("precoOriginal", "29.90").param("preco", "29.90").param("estoque", "10").param("condicao", "NOVO")
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



