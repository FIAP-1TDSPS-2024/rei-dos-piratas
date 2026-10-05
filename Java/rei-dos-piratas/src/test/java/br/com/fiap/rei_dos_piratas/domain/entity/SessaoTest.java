package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class SessaoTest {
    private final Instant agora = Instant.parse("2026-10-05T12:00:00Z");
    private Sessao sessao() {
        return new Sessao(UUID.randomUUID(), new IdentidadeConta(TipoConta.CLIENTE, 1L),
                agora, agora.plusSeconds(604800), null, "hash-atual", Set.of());
    }

    @Test
    void prazoAbsolutoNaoSeEstendeNaRotacaoEExpiraNoLimiteExato() {
        Sessao sessao = sessao();
        sessao.validarRefresh("hash-atual", agora.plusSeconds(604799));
        sessao.rotacionar("novo-hash");
        assertThat(sessao.getExpiraEm()).isEqualTo(agora.plusSeconds(604800));
        assertThat(sessao.valida(agora.plusSeconds(604800))).isFalse();
        assertThatThrownBy(() -> sessao.validarRefresh("novo-hash", sessao.getExpiraEm()))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void reutilizacaoDeQualquerHashAnteriorRevogaSessao() {
        Sessao sessao = sessao();
        sessao.rotacionar("segundo");
        sessao.rotacionar("terceiro");
        assertThatThrownBy(() -> sessao.validarRefresh("hash-atual", agora))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThat(sessao.valida(agora)).isFalse();
        assertThat(sessao.getRevogadaEm()).isEqualTo(agora);
    }

    @Test
    void hashDesconhecidoNaoRevogaESairMantemPrimeiraDataDeRevogacao() {
        Sessao sessao = sessao();
        assertThatThrownBy(() -> sessao.validarRefresh("desconhecido", agora))
                .isInstanceOf(CredenciaisInvalidasException.class);
        assertThat(sessao.valida(agora)).isTrue();
        sessao.revogar(agora);
        sessao.revogar(agora.plusSeconds(1));
        assertThat(sessao.getRevogadaEm()).isEqualTo(agora);
        assertThatThrownBy(() -> sessao.validarRefresh("hash-atual", agora))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
