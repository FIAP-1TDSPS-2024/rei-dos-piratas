package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebAuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtil jwt;
    private final Map<String, Cookie> jar = new HashMap<>();
    private String csrfToken;
    private String csrfHeader;

    @BeforeEach
    void funcionario() {
        em.createNativeQuery("INSERT INTO FUNCIONARIOS (id, user_name, nome_completo, email, senha, usuario_ativo, data_cadastro, perfil_id) "
                + "VALUES (9200, 'web_staff', 'Funcionario Web', 'web@example.com', :senha, true, CURRENT_DATE, 2)")
                .setParameter("senha", encoder.encode("SenhaSegura123")).executeUpdate();
    }

    private MvcResult executar(MockHttpServletRequestBuilder request, int status) throws Exception {
        if (!jar.isEmpty()) request.cookie(jar.values().toArray(Cookie[]::new));
        MvcResult result = mvc.perform(request).andExpect(status().is(status)).andReturn();
        for (Cookie cookie : result.getResponse().getCookies()) {
            if (cookie.getMaxAge() == 0) jar.remove(cookie.getName());
            else jar.put(cookie.getName(), cookie);
        }
        return result;
    }

    private void csrf() throws Exception {
        var response = executar(get("/web/auth/csrf"), 200);
        var json = mapper.readTree(response.getResponse().getContentAsString());
        csrfToken = json.get("token").asText();
        csrfHeader = json.get("headerName").asText();
    }

    private MvcResult login() throws Exception {
        csrf();
        return executar(post("/web/auth/login").header(csrfHeader, csrfToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"web@example.com\",\"password\":\"SenhaSegura123\"}"), 204);
    }

    @Test
    void loginWebNaoExpoeTokensECookiesPossuemFlagsEPrazos() throws Exception {
        MvcResult result = login();
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(result.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(jar.get("jwt_token").isHttpOnly()).isTrue();
        assertThat(jar.get("refresh_token").isHttpOnly()).isTrue();
        assertThat(jar.get("auth_expires_at").isHttpOnly()).isFalse();
        assertThat(jar.get("jwt_token").getMaxAge()).isBetween(890, 900);
        assertThat(jar.get("refresh_token").getMaxAge()).isBetween(604790, 604800);
        assertThat(jar.get("refresh_token").getPath()).isEqualTo("/web");
        assertThat(result.getResponse().getHeaders("Set-Cookie"))
                .filteredOn(value -> !value.startsWith("XSRF-TOKEN="))
                .allMatch(value -> value.contains("SameSite=Strict"));
        executar(get("/web/produtos"), 200);
        csrf();
        assertThat(jar.get("XSRF-TOKEN").isHttpOnly()).isTrue();
    }

    @Test
    void loginCadastroRefreshELogoutExigemCsrfReal() throws Exception {
        executar(post("/web/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"web@example.com\",\"password\":\"SenhaSegura123\"}"), 403);
        executar(post("/web/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("{}"), 403);
        login();
        executar(post("/web/auth/refresh"), 403);
        executar(post("/web/logout"), 403);
        csrf();
        executar(post("/web/auth/refresh").header(csrfHeader, "incorreto"), 403);
        executar(post("/web/auth/refresh").header(csrfHeader, csrfToken), 204);
    }

    @Test
    void renovacaoWebUsaCookieAtualSemDevolverSegredos() throws Exception {
        login();
        String anterior = jar.get("refresh_token").getValue();
        var id = jwt.extractSessaoId(jar.get("jwt_token").getValue());
        csrf();
        var result = executar(post("/web/auth/refresh").header(csrfHeader, csrfToken), 204);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(jar.get("refresh_token").getValue()).isNotEqualTo(anterior);
        assertThat(jwt.extractSessaoId(jar.get("jwt_token").getValue())).isEqualTo(id);
        assertThat(result.getResponse().getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    void apiNaoAceitaCookiesEWebNaoUsaBearerParaContornarCsrf() throws Exception {
        login();
        String access = jar.get("jwt_token").getValue();
        csrf();
        mvc.perform(post("/web/logout").cookie(jar.get("XSRF-TOKEN"))
                        .header(csrfHeader, csrfToken).header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
        executar(post("/auth/logout"), 401);
        executar(post("/auth/logout").header("Authorization", "Bearer " + access), 200);
        executar(post("/web/logout").header("Authorization", "Bearer " + access), 403);
    }

    @Test
    void logoutWebRemoveCookiesERevogaSessao() throws Exception {
        login();
        String access = jar.get("jwt_token").getValue();
        csrf();
        executar(post("/web/logout").header(csrfHeader, csrfToken), 302);
        assertThat(jar).doesNotContainKeys("jwt_token", "refresh_token", "auth_expires_at", "XSRF-TOKEN");
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiracaoDoAccessEncaminhaPaginaParaRenovacaoERestauraAcesso() throws Exception {
        login();
        var id = jwt.extractSessaoId(jar.get("jwt_token").getValue());
        jar.put("jwt_token", new Cookie("jwt_token", jwt.createToken(Map.of("tipo", "FUNCIONARIO",
                "uid", 9200, "sid", id.toString()), "FUNCIONARIO:9200", -1000L)));
        var result = executar(get("/web/pedidos").accept(MediaType.TEXT_HTML), 302);
        assertThat(result.getResponse().getRedirectedUrl()).startsWith("/web/renovar?destino=");
        csrf();
        executar(post("/web/auth/refresh").header(csrfHeader, csrfToken), 204);
        executar(get("/web/produtos"), 200);
    }

    @Test
    void refreshRevogadoLimpaCookiesERetorna401() throws Exception {
        login();
        String access = jar.get("jwt_token").getValue();
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + access)).andExpect(status().isOk());
        // A transacao do teste engloba varias requisicoes; confirma o dirty checking antes do refresh com lock.
        em.flush();
        csrf();
        executar(post("/web/auth/refresh").header(csrfHeader, csrfToken), 401);
        assertThat(jar).doesNotContainKeys("jwt_token", "refresh_token", "auth_expires_at");
    }

    @Test
    void formularioThymeleafPossuiCsrfERedirecionamentoNaoAceitaDestinoExterno() throws Exception {
        login();
        var pagina = executar(get("/web/produtos"), 200).getResponse().getContentAsString();
        assertThat(pagina).contains("name=\"_csrf\"", "/assets/web-auth.js");
        mvc.perform(get("/web/renovar").param("destino", "https://evil.example"))
                .andExpect(status().isOk()).andExpect(model().attribute("destino", "/web/produtos"));
    }

    @Test
    void cadastroWebCriaSessaoComCookiesSemExporCredenciais() throws Exception {
        csrf();
        var result = executar(post("/web/auth/cadastro").header(csrfHeader, csrfToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_web", "nome_completo":"Cliente Web", "email":"cadastro-web@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """), 201);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(jar).containsKeys("jwt_token", "refresh_token", "auth_expires_at");
        assertThat(jwt.extractIdentidade(jar.get("jwt_token").getValue()).tipo().name()).isEqualTo("CLIENTE");
        csrf();
        executar(post("/web/auth/refresh").header(csrfHeader, csrfToken), 204);
    }

    @Test
    void corsWebBloqueiaOutraOrigemEApiExigeOrigemConfigurada() throws Exception {
        mvc.perform(get("/web/auth/csrf").header("Origin", "https://evil.vercel.app"))
                .andExpect(status().isForbidden());
        mvc.perform(options("/auth/login").header("Origin", "https://evil.vercel.app")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
        mvc.perform(options("/auth/login").header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isOk());
    }
}
