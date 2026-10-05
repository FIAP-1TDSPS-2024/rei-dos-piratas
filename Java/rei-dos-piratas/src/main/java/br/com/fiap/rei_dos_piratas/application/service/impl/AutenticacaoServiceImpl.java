package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.application.service.TokenAcessoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import br.com.fiap.rei_dos_piratas.domain.repository.SessaoRepository;
import br.com.fiap.rei_dos_piratas.application.service.RefreshTokenService;
import java.time.Clock;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class AutenticacaoServiceImpl implements AutenticacaoService {
    private final ContaRepository contas;
    private final SenhaService senhas;
    private final TokenAcessoService tokens;
    private final ClienteService clientes;
    private final SessaoRepository sessoes;
    private final RefreshTokenService refreshTokens;
    private final Clock clock;

    public AutenticacaoServiceImpl(ContaRepository contas, SenhaService senhas, TokenAcessoService tokens,
                                   ClienteService clientes, SessaoRepository sessoes,
                                   RefreshTokenService refreshTokens, Clock clock) {
        this.contas = contas;
        this.senhas = senhas;
        this.tokens = tokens;
        this.clientes = clientes;
        this.sessoes = sessoes;
        this.refreshTokens = refreshTokens;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Resultado login(String email, String senha) {
        Conta conta;
        try {
            conta = contas.findByEmail(Usuario.normalizarEmail(email))
                    .orElseThrow(CredenciaisInvalidasException::new);
            if (!senhas.matches(senha, conta.usuario().getSenha())) {
                throw new CredenciaisInvalidasException();
            }
            conta.usuario().validarAcesso();
        } catch (CredenciaisInvalidasException e) {
            log.warn("[AUTENTICACAO] Login recusado - credenciais invalidas ou conta inativa");
            throw e;
        }
        Resultado resultado = abrirSessao(conta, "LOGIN");
        log.info("[AUTENTICACAO] Login realizado - usuarioId={}, tipoConta={}",
                conta.identidade().id(), conta.identidade().tipo());
        return resultado;
    }

    @Override
    @Transactional
    public Resultado cadastrar(Cliente cliente) {
        Cliente criado = clientes.create(cliente);
        criado.validarAcesso();
        Conta conta = new Conta(new IdentidadeConta(TipoConta.CLIENTE, criado.getId()), criado);
        Resultado resultado = abrirSessao(conta, "CADASTRO");
        log.info("[AUTENTICACAO] Sessao inicial do cadastro criada - usuarioId={}, tipoConta={}",
                conta.identidade().id(), conta.identidade().tipo());
        return resultado;
    }

    private Resultado abrirSessao(Conta conta, String origem) {
        UUID id = UUID.randomUUID();
        String refresh = refreshTokens.gerar(id);
        var agora = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        Sessao sessao = new Sessao(id, conta.identidade(), agora, agora.plus(Duration.ofDays(7)),
                null, refreshTokens.hash(refresh), Set.of());
        sessoes.salvar(sessao);
        Resultado resultado = resultado(conta, sessao, refresh);
        log.info("[SESSAO] Sessao aberta - sessaoId={}, usuarioId={}, tipoConta={}, origem={}, expiraEm={}",
                id, conta.identidade().id(), conta.identidade().tipo(), origem, sessao.getExpiraEm());
        return resultado;
    }

    @Override
    @Transactional(noRollbackFor = CredenciaisInvalidasException.class)
    public Resultado renovar(String refreshToken) {
        UUID id;
        try {
            id = refreshTokens.identificarSessao(refreshToken);
        } catch (CredenciaisInvalidasException e) {
            log.debug("[SESSAO] Renovacao recusada - refresh ausente ou malformado");
            throw e;
        }
        Sessao sessao = sessoes.findByIdParaAtualizar(id).orElseThrow(() -> {
            log.debug("[SESSAO] Renovacao recusada - sessaoId={}, motivo=SESSAO_INEXISTENTE", id);
            return new CredenciaisInvalidasException();
        });
        boolean jaRevogada = sessao.getRevogadaEm() != null;
        try {
            sessao.validarRefresh(refreshTokens.hash(refreshToken), clock.instant());
        } catch (CredenciaisInvalidasException e) {
            // A revogacao por reutilizacao precisa ser confirmada mesmo retornando 401.
            if (sessao.getRevogadaEm() != null) sessoes.salvar(sessao);
            if (!jaRevogada && sessao.getRevogadaEm() != null) {
                log.warn("[SESSAO] Refresh reutilizado; sessao revogada por seguranca - sessaoId={}, usuarioId={}, tipoConta={}",
                        id, sessao.getIdentidade().id(), sessao.getIdentidade().tipo());
            } else {
                log.debug("[SESSAO] Renovacao recusada - sessaoId={}, motivo={}", id,
                        jaRevogada ? "SESSAO_REVOGADA" : !sessao.valida(clock.instant()) ? "SESSAO_EXPIRADA" : "REFRESH_INCORRETO");
            }
            throw e;
        }
        Conta conta;
        try {
            conta = contas.findByIdentidade(sessao.getIdentidade())
                    .orElseThrow(CredenciaisInvalidasException::new);
            conta.usuario().validarAcesso();
        } catch (CredenciaisInvalidasException e) {
            log.debug("[SESSAO] Renovacao recusada - sessaoId={}, usuarioId={}, tipoConta={}, motivo=CONTA_INDISPONIVEL",
                    id, sessao.getIdentidade().id(), sessao.getIdentidade().tipo());
            throw e;
        }
        String novoRefresh = refreshTokens.gerar(id);
        sessao.rotacionar(refreshTokens.hash(novoRefresh));
        sessoes.salvar(sessao);
        Resultado resultado = resultado(conta, sessao, novoRefresh);
        log.info("[SESSAO] Sessao renovada - sessaoId={}, usuarioId={}, tipoConta={}, expiraEm={}",
                id, sessao.getIdentidade().id(), sessao.getIdentidade().tipo(), sessao.getExpiraEm());
        return resultado;
    }

    @Override
    @Transactional
    public void logout(UUID sessaoId) {
        Sessao sessao = sessoes.findByIdParaAtualizar(sessaoId).orElseThrow(CredenciaisInvalidasException::new);
        sessao.revogar(clock.instant());
        sessoes.salvar(sessao);
        log.info("[SESSAO] Sessao revogada por logout - sessaoId={}, usuarioId={}, tipoConta={}",
                sessaoId, sessao.getIdentidade().id(), sessao.getIdentidade().tipo());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean sessaoValida(UUID sessaoId, IdentidadeConta identidade) {
        boolean valida = sessoes.findById(sessaoId)
                .filter(sessao -> sessao.getIdentidade().equals(identidade) && sessao.valida(clock.instant()))
                .isPresent();
        if (!valida) {
            log.debug("[SESSAO] Acesso recusado - sessaoId={}, usuarioId={}, tipoConta={}, motivo=SESSAO_INVALIDA",
                    sessaoId, identidade.id(), identidade.tipo());
        }
        return valida;
    }

    private Resultado resultado(Conta conta, Sessao sessao, String refresh) {
        return new Resultado(tokens.emitir(conta, sessao), conta, refresh, sessao.getExpiraEm());
    }
}
