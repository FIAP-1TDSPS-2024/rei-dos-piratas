package br.com.fiap.rei_dos_piratas.integration;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import br.com.fiap.rei_dos_piratas.infrastructure.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacaoIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtil jwt;
    @Autowired ContaRepository contas;
    @Autowired br.com.fiap.rei_dos_piratas.domain.repository.PerfilRepository perfis;
    @Autowired br.com.fiap.rei_dos_piratas.application.service.ClienteService clientes;
    @Autowired br.com.fiap.rei_dos_piratas.application.service.FuncionarioService funcionarios;

    private void funcionario(boolean ativo) {
        em.createNativeQuery("INSERT INTO FUNCIONARIOS (id, user_name, nome_completo, email, senha, usuario_ativo, data_cadastro, perfil_id) "
                        + "VALUES (9001, 'staff_auth', 'Funcionario Teste', 'staff@example.com', :senha, :ativo, CURRENT_DATE, 2)")
                .setParameter("senha", encoder.encode("SenhaSegura123")).setParameter("ativo", ativo).executeUpdate();
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"STAFF@example.com\",\"password\":\"SenhaSegura123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.funcionario.id").value(9001))
                .andExpect(jsonPath("$.cliente").isEmpty()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    @Test
    void loginIdentificaFuncionarioELogoutRevogaTokenAtual() throws Exception {
        funcionario(true);
        String token = login();
        String outroLogin = login();
        assertThat(outroLogin).isNotEqualTo(token);
        assertThat(jwt.extractIdentidade(token)).isEqualTo(new IdentidadeConta(TipoConta.FUNCIONARIO, 9001L));
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/auth/privado").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + outroLogin)).andExpect(status().isOk());
    }

    @Test
    void cadastroCriaSessaoInicialERefreshUtilizavel() throws Exception {
        String body = mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_sessao", "nome_completo":"Cliente Sessao", "email":"cadastro@example.com",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var dados = mapper.readTree(body);
        assertThat(jwt.extractIdentidade(dados.get("token").asText()).tipo()).isEqualTo(TipoConta.CLIENTE);
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refresh_token", dados.get("refresh_token").asText()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cliente.email").value("cadastro@example.com"));
    }

    @Test
    void desativacaoBloqueiaJwtJaEmitidoELoginRetorna401() throws Exception {
        funcionario(true);
        String token = login();
        em.createNativeQuery("UPDATE FUNCIONARIOS SET usuario_ativo = false WHERE id = 9001").executeUpdate();
        em.clear();
        mvc.perform(get("/auth/privado").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"staff@example.com\",\"password\":\"SenhaSegura123\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void rotasPrivadasExigemAutenticacaoETokensAntigosOuExpiradosSaoRejeitados() throws Exception {
        mvc.perform(post("/auth/logout")).andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/privado")).andExpect(status().isUnauthorized());
        String antigo = jwt.createToken(Map.of(), "staff@example.com", 60000L);
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + antigo)).andExpect(status().isUnauthorized());
        String expirado = jwt.createToken(Map.of("tipo", "FUNCIONARIO", "uid", 9001L), "FUNCIONARIO:9001", -1000L);
        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + expirado)).andExpect(status().isUnauthorized());
    }

    @Test
    void emailDeFuncionarioNaoPodeSerUsadoPorClienteMesmoComMaiusculas() throws Exception {
        funcionario(false);
        mvc.perform(post("/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"user_name":"cliente_auth", "nome_completo":"Cliente Teste", "email":"STAFF@EXAMPLE.COM",
                 "senha":"SenhaSegura123", "data_nascimento":"1990-01-01", "sexo":"M",
                 "cpf":"52998224725", "celular":"11987654321"}
                """)).andExpect(status().isBadRequest());
        assertThat(contas.emailOcupado("staff@example.com", new IdentidadeConta(TipoConta.CLIENTE, 9001L))).isTrue();
        assertThat(contas.emailOcupado("staff@example.com", new IdentidadeConta(TipoConta.FUNCIONARIO, 9001L))).isFalse();
    }

    @Test
    void emailDeClienteImpedeCadastroDeFuncionario() {
        clientes.create(new br.com.fiap.rei_dos_piratas.domain.entity.Cliente("client_auth", "Cliente Teste",
                " CLIENT@EXAMPLE.COM ", "SenhaSegura123", java.time.LocalDate.of(1990, 1, 1),
                br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum.M, "52998224725", "11987654321"));
        var perfil = perfis.findByNome("USER");
        var novo = new br.com.fiap.rei_dos_piratas.domain.entity.Funcionario("novo_staff", "Funcionario Teste",
                "client@example.com", "SenhaSegura123", perfil, null);
        assertThatThrownBy(() -> funcionarios.create(novo))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException.class);
    }

    @Test
    void alteracaoDeEmailNaoPodeUsarEmailDeOutraTabelaEProprioEmailPermaneceValido() {
        funcionario(true);
        clientes.create(new br.com.fiap.rei_dos_piratas.domain.entity.Cliente("client_auth", "Cliente Teste",
                "client@example.com", "SenhaSegura123", java.time.LocalDate.of(1990, 1, 1),
                br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum.M, "52998224725", "11987654321"));
        var perfil = perfis.findByNome("USER");
        var atualizacao = new br.com.fiap.rei_dos_piratas.domain.entity.Funcionario("staff_auth", 9001L, "Funcionario Teste",
                "STAFF@example.com", "SenhaSegura123", true, java.time.LocalDate.now(), perfil, null, null);
        assertThat(funcionarios.update(atualizacao).getEmail()).isEqualTo("staff@example.com");
        atualizacao.setEmail("client@example.com");
        atualizacao.setSenha("SenhaSegura123");
        assertThatThrownBy(() -> funcionarios.update(atualizacao))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.UniqueKeyDuplicatedException.class);
    }
}
