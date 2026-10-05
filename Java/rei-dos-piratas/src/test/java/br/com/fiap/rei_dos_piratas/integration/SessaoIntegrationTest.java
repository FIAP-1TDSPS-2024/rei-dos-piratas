package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SessaoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtil jwt;
    @Autowired AutenticacaoService autenticacao;

    private void transacao(Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
    }

    @BeforeEach
    void preparar() {
        limpar();
        transacao(() -> em.createNativeQuery("INSERT INTO FUNCIONARIOS "
                + "(id, user_name, nome_completo, email, senha, usuario_ativo, data_cadastro, perfil_id) "
                + "VALUES (9100, 'session_staff', 'Funcionario Sessao', 'session@example.com', :senha, true, CURRENT_DATE, 2)")
                .setParameter("senha", encoder.encode("SenhaSegura123")).executeUpdate());
    }

    @AfterEach
    void limpar() {
        transacao(() -> {
            em.createNativeQuery("DELETE FROM AUTH_REFRESH_USADOS WHERE sessao_id IN "
                    + "(SELECT id FROM AUTH_SESSOES WHERE usuario_id = 9100)").executeUpdate();
            em.createNativeQuery("DELETE FROM AUTH_SESSOES WHERE usuario_id = 9100").executeUpdate();
            em.createNativeQuery("DELETE FROM FUNCIONARIOS WHERE id = 9100").executeUpdate();
        });
    }

    private JsonNode login() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"session@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private JsonNode renovar(String refresh) throws Exception {
        String body = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refresh_token", refresh))))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private void refreshNegado(String refresh) throws Exception {
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("refresh_token", refresh))))
                .andExpect(status().isUnauthorized());
    }

    private void acessoNegado(String token) throws Exception {
        mvc.perform(get("/auth/privado").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotacaoMantemSessaoPrazoAbsolutoEArmazenaSomenteHashes() throws Exception {
        JsonNode inicial = login();
        String token = inicial.get("token").asText();
        String refresh = inicial.get("refresh_token").asText();
        UUID id = jwt.extractSessaoId(token);
        var claims = jwt.extractAllClaims(token);
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(900000);
        JsonNode novo = renovar(refresh);
        assertThat(novo.get("refresh_token").asText()).isNotEqualTo(refresh);
        assertThat(jwt.extractSessaoId(novo.get("token").asText())).isEqualTo(id);
        assertThat(novo.get("refresh_expira_em")).isEqualTo(inicial.get("refresh_expira_em"));
        transacao(() -> {
            em.clear();
            String hash = (String) em.createNativeQuery("SELECT refresh_hash FROM AUTH_SESSOES WHERE id = :id")
                    .setParameter("id", id).getSingleResult();
            assertThat(hash).hasSize(64).isNotEqualTo(refresh).isNotEqualTo(novo.get("refresh_token").asText());
            assertThat(((Number) em.createNativeQuery("SELECT COUNT(*) FROM AUTH_REFRESH_USADOS WHERE sessao_id = :id")
                    .setParameter("id", id).getSingleResult()).intValue()).isEqualTo(1);
        });
    }

    @Test
    void reutilizacaoAntigaRevogaSessaoEConfirmaRevogacaoMesmoRetornando401() throws Exception {
        JsonNode inicial = login();
        JsonNode primeiro = renovar(inicial.get("refresh_token").asText());
        JsonNode segundo = renovar(primeiro.get("refresh_token").asText());
        refreshNegado(inicial.get("refresh_token").asText());
        refreshNegado(segundo.get("refresh_token").asText());
        acessoNegado(inicial.get("token").asText());
        acessoNegado(segundo.get("token").asText());
        transacao(() -> {
            em.clear();
            assertThat(em.createNativeQuery("SELECT revogada_em FROM AUTH_SESSOES WHERE id = :id")
                    .setParameter("id", jwt.extractSessaoId(inicial.get("token").asText())).getSingleResult()).isNotNull();
        });
    }

    @Test
    void logoutRevogaRefreshEAcessosDaSessaoSemAfetarOutroLogin() throws Exception {
        JsonNode primeiro = login();
        JsonNode segundo = login();
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + primeiro.get("token").asText()))
                .andExpect(status().isOk());
        acessoNegado(primeiro.get("token").asText());
        refreshNegado(primeiro.get("refresh_token").asText());
        renovar(segundo.get("refresh_token").asText());
    }

    @Test
    void logoutWebRevogaMesmaSessao() throws Exception {
        JsonNode inicial = login();
        var csrfResponse = mvc.perform(get("/web/auth/csrf")).andExpect(status().isOk()).andReturn().getResponse();
        var csrfDados = mapper.readTree(csrfResponse.getContentAsString());
        mvc.perform(post("/web/logout")
                        .header(csrfDados.get("headerName").asText(), csrfDados.get("token").asText())
                        .cookie(csrfResponse.getCookie("XSRF-TOKEN"),
                                new jakarta.servlet.http.Cookie("jwt_token", inicial.get("token").asText())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/web/login?logout"))
                .andExpect(cookie().maxAge("jwt_token", 0));
        refreshNegado(inicial.get("refresh_token").asText());
    }

    @Test
    void tokenIncorretoNaoPermiteRevogarSessaoAlheia() throws Exception {
        JsonNode inicial = login();
        refreshNegado(jwt.extractSessaoId(inicial.get("token").asText()) + "." + "A".repeat(43));
        renovar(inicial.get("refresh_token").asText());
        refreshNegado(UUID.randomUUID() + "." + "A".repeat(43));
        refreshNegado("token-invalido");
    }

    @Test
    void accessExpiradoPodeSerRenovadoComRefreshValido() throws Exception {
        JsonNode inicial = login();
        String expirado = jwt.createToken(Map.of("tipo", "FUNCIONARIO", "uid", 9100,
                "sid", jwt.extractSessaoId(inicial.get("token").asText()).toString()), "FUNCIONARIO:9100", -1000L);
        acessoNegado(expirado);
        renovar(inicial.get("refresh_token").asText());
    }

    @Test
    void sessaoExpiradaOuContaInativaImpedeRenovacaoEAcesso() throws Exception {
        JsonNode expirada = login();
        transacao(() -> em.createNativeQuery("UPDATE AUTH_SESSOES SET criada_em = :criada, expira_em = :expira WHERE id = :id")
                .setParameter("criada", Instant.now().minusSeconds(604800)).setParameter("expira", Instant.now().minusSeconds(1))
                .setParameter("id", jwt.extractSessaoId(expirada.get("token").asText())).executeUpdate());
        refreshNegado(expirada.get("refresh_token").asText());
        acessoNegado(expirada.get("token").asText());
        JsonNode inativa = login();
        transacao(() -> em.createNativeQuery("UPDATE FUNCIONARIOS SET usuario_ativo = false WHERE id = 9100").executeUpdate());
        refreshNegado(inativa.get("refresh_token").asText());
        acessoNegado(inativa.get("token").asText());
    }

    @Test
    void sessaoPrecisaPertencerAIdentidadeDoJwt() throws Exception {
        JsonNode inicial = login();
        String falso = jwt.createToken(Map.of("tipo", "FUNCIONARIO", "uid", 9100,
                "sid", UUID.randomUUID().toString()), "FUNCIONARIO:9100", 900000L);
        acessoNegado(falso);
        String semSessao = jwt.createToken(Map.of("tipo", "FUNCIONARIO", "uid", 9100), "FUNCIONARIO:9100", 900000L);
        acessoNegado(semSessao);
        transacao(() -> em.createNativeQuery("UPDATE AUTH_SESSOES SET tipo_conta = 'CLIENTE' WHERE id = :id")
                .setParameter("id", jwt.extractSessaoId(inicial.get("token").asText())).executeUpdate());
        acessoNegado(inicial.get("token").asText());
    }

    @Test
    void renovacoesConcorrentesDoMesmoTokenRevogamSessaoPorReutilizacao() throws Exception {
        JsonNode inicial = login();
        String refresh = inicial.get("refresh_token").asText();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        Callable<Boolean> tarefa = () -> {
            iniciar.await();
            try { autenticacao.renovar(refresh); return true; }
            catch (br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException e) { return false; }
        };
        try {
            Future<Boolean> a = executor.submit(tarefa);
            Future<Boolean> b = executor.submit(tarefa);
            iniciar.countDown();
            assertThat(java.util.List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            acessoNegado(inicial.get("token").asText());
        } finally { executor.shutdownNow(); }
    }
}
