package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.application.service.TokenAcessoService;
import br.com.fiap.rei_dos_piratas.application.service.RefreshTokenService;
import br.com.fiap.rei_dos_piratas.domain.repository.SessaoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.RefreshTokenServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoServiceImplTest {
    private final Logger logger = (Logger) LoggerFactory.getLogger(AutenticacaoServiceImpl.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private Level nivelAnterior;

    @BeforeEach
    void capturarLogs() {
        nivelAnterior = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void liberarLogs() {
        logger.detachAppender(logs);
        logs.stop();
        logger.setLevel(nivelAnterior);
    }

    private String mensagens() {
        return logs.list.stream().map(ILoggingEvent::getFormattedMessage)
                .collect(java.util.stream.Collectors.joining("\n"));
    }
    private final ContaRepository contas = mock(ContaRepository.class);
    private final SenhaService senhas = mock(SenhaService.class);
    private final TokenAcessoService tokens = mock(TokenAcessoService.class);
    private final ClienteService clientes = mock(ClienteService.class);
    private final SessaoRepository sessoes = mock(SessaoRepository.class);
    private final RefreshTokenService refresh = new RefreshTokenServiceImpl();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC);
    private final AutenticacaoServiceImpl service = new AutenticacaoServiceImpl(contas, senhas, tokens, clientes,
            sessoes, refresh, clock);

    @Test
    void funcionarioSemPermissaoDeProdutoContinuaSendoFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setUsuarioAtivo(true);
        funcionario.setSenha("hash");
        funcionario.setPerfil(new Perfil(1L, "USER", "", List.of()));
        Conta conta = new Conta(new IdentidadeConta(TipoConta.FUNCIONARIO, 1L), funcionario);
        when(contas.findByEmail("staff@example.com")).thenReturn(Optional.of(conta));
        when(senhas.matches("senha", "hash")).thenReturn(true);
        when(tokens.emitir(eq(conta), any())).thenReturn("jwt");
        var resultado = service.login(" STAFF@Example.com ", "senha");
        assertThat(resultado.conta().identidade().tipo()).isEqualTo(TipoConta.FUNCIONARIO);
        assertThat(resultado.token()).isEqualTo("jwt");
        assertThat(resultado.refreshExpiraEm()).isEqualTo(clock.instant().plusSeconds(604800));
        var captor = org.mockito.ArgumentCaptor.forClass(Sessao.class);
        verify(sessoes).salvar(captor.capture());
        assertThat(captor.getValue().getRefreshHash()).isEqualTo(refresh.hash(resultado.refreshToken()));
        assertThat(captor.getValue().getRefreshHash()).isNotEqualTo(resultado.refreshToken());
        assertThat(mensagens()).contains("[AUTENTICACAO] Login realizado", "[SESSAO] Sessao aberta",
                        "sessaoId=" + captor.getValue().getId(), "usuarioId=1", "tipoConta=FUNCIONARIO")
                .doesNotContain("staff@example.com", "senha", "hash", resultado.token(),
                        resultado.refreshToken(), captor.getValue().getRefreshHash());
        verifyNoInteractions(clientes);
    }

    @Test
    void contaInativaOuSenhaIncorretaNaoEmiteToken() {
        Cliente cliente = new Cliente();
        cliente.setSenha("hash");
        Conta conta = new Conta(new IdentidadeConta(TipoConta.CLIENTE, 1L), cliente);
        when(contas.findByEmail("c@example.com")).thenReturn(Optional.of(conta));
        when(senhas.matches("senha", "hash")).thenReturn(true);
        assertThatThrownBy(() -> service.login("c@example.com", "senha")).isInstanceOf(CredenciaisInvalidasException.class);
        cliente.setUsuarioAtivo(true);
        assertThatThrownBy(() -> service.login("c@example.com", "errada")).isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> service.login("ausente@example.com", "senha")).isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(tokens, sessoes);
        assertThat(logs.list).hasSize(3).allMatch(event -> event.getLevel() == Level.WARN);
        assertThat(mensagens()).contains("[AUTENTICACAO] Login recusado")
                .doesNotContain("c@example.com", "ausente@example.com", "senha", "errada", "hash");
    }

    @Test
    void cadastroUsaContaCriadaSemAutenticarNovamente() {
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuarioAtivo(true);
        when(clientes.create(cliente)).thenReturn(cliente);
        when(tokens.emitir(any(), any())).thenReturn("jwt");
        var resultado = service.cadastrar(cliente);
        assertThat(resultado.conta().identidade()).isEqualTo(new IdentidadeConta(TipoConta.CLIENTE, 2L));
        verifyNoInteractions(contas, senhas);
        assertThat(mensagens()).contains("origem=CADASTRO", "[AUTENTICACAO] Sessao inicial do cadastro criada");
    }

    private Sessao sessao(UUID id, String refreshToken, Instant expiraEm, Instant revogadaEm, Set<String> usados) {
        return new Sessao(id, new IdentidadeConta(TipoConta.CLIENTE, 2L), clock.instant().minusSeconds(60),
                expiraEm, revogadaEm, refresh.hash(refreshToken), usados);
    }

    @Test
    void refreshReutilizadoRegistraRevogacaoSemExporSegredos() {
        UUID id = UUID.randomUUID();
        String anterior = refresh.gerar(id);
        String atual = refresh.gerar(id);
        Sessao sessao = sessao(id, atual, clock.instant().plusSeconds(60), null, Set.of(refresh.hash(anterior)));
        when(sessoes.findByIdParaAtualizar(id)).thenReturn(Optional.of(sessao));
        assertThatThrownBy(() -> service.renovar(anterior)).isInstanceOf(CredenciaisInvalidasException.class);
        verify(sessoes).salvar(sessao);
        assertThat(logs.list).singleElement().satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
        assertThat(mensagens()).contains("[SESSAO] Refresh reutilizado; sessao revogada por seguranca", "sessaoId=" + id)
                .doesNotContain(anterior, atual, refresh.hash(anterior), refresh.hash(atual));
    }

    @Test
    void expiracaoERevogacaoPreviaNaoSaoConfundidasComReutilizacao() {
        UUID id = UUID.randomUUID();
        String token = refresh.gerar(id);
        Sessao expirada = sessao(id, token, clock.instant(), null, Set.of());
        when(sessoes.findByIdParaAtualizar(id)).thenReturn(Optional.of(expirada));
        assertThatThrownBy(() -> service.renovar(token)).isInstanceOf(CredenciaisInvalidasException.class);
        assertThat(expirada.getRevogadaEm()).isNull();
        verify(sessoes, never()).salvar(any());
        Sessao revogada = sessao(id, token, clock.instant().plusSeconds(60), clock.instant(), Set.of());
        when(sessoes.findByIdParaAtualizar(id)).thenReturn(Optional.of(revogada));
        assertThatThrownBy(() -> service.renovar(token)).isInstanceOf(CredenciaisInvalidasException.class);
        assertThat(logs.list).hasSize(2).allMatch(event -> event.getLevel() == Level.DEBUG);
        assertThat(mensagens()).contains("motivo=SESSAO_EXPIRADA", "motivo=SESSAO_REVOGADA")
                .doesNotContain("Refresh reutilizado", token, refresh.hash(token));
    }

    @Test
    void renovacaoELogoutRegistramMesmaSessaoSemCredenciais() {
        UUID id = UUID.randomUUID();
        String token = refresh.gerar(id);
        Sessao sessao = sessao(id, token, clock.instant().plusSeconds(60), null, Set.of());
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuarioAtivo(true);
        Conta conta = new Conta(sessao.getIdentidade(), cliente);
        when(sessoes.findByIdParaAtualizar(id)).thenReturn(Optional.of(sessao));
        when(contas.findByIdentidade(sessao.getIdentidade())).thenReturn(Optional.of(conta));
        when(tokens.emitir(eq(conta), eq(sessao))).thenReturn("access-secreto");
        var resultado = service.renovar(token);
        service.logout(id);
        assertThat(mensagens()).contains("[SESSAO] Sessao renovada", "[SESSAO] Sessao revogada por logout",
                        "sessaoId=" + id, "usuarioId=2", "tipoConta=CLIENTE")
                .doesNotContain(token, resultado.refreshToken(), resultado.token(), sessao.getRefreshHash());
    }

    @Test
    void acessoValidoNaoProduzLogPorRequisicaoEAcessoInvalidoUsaDebug() {
        UUID id = UUID.randomUUID();
        Sessao sessao = sessao(id, refresh.gerar(id), clock.instant().plusSeconds(60), null, Set.of());
        when(sessoes.findById(id)).thenReturn(Optional.of(sessao));
        assertThat(service.sessaoValida(id, sessao.getIdentidade())).isTrue();
        assertThat(logs.list).isEmpty();
        sessao.revogar(clock.instant());
        assertThat(service.sessaoValida(id, sessao.getIdentidade())).isFalse();
        assertThat(logs.list).singleElement().satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.DEBUG));
        assertThat(mensagens()).contains("[SESSAO] Acesso recusado", "sessaoId=" + id);
    }
}
