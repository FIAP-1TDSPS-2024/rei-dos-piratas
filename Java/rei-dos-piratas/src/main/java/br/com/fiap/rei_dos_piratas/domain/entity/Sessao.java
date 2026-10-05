package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import lombok.Getter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Sessao {
    private final UUID id;
    private final IdentidadeConta identidade;
    private final Instant criadaEm;
    private final Instant expiraEm;
    private Instant revogadaEm;
    private String refreshHash;
    private final Set<String> refreshUsados;

    public Sessao(UUID id, IdentidadeConta identidade, Instant criadaEm, Instant expiraEm,
                  Instant revogadaEm, String refreshHash, Set<String> refreshUsados) {
        this.id = id;
        this.identidade = identidade;
        this.criadaEm = criadaEm;
        this.expiraEm = expiraEm;
        this.revogadaEm = revogadaEm;
        this.refreshHash = refreshHash;
        this.refreshUsados = new HashSet<>(refreshUsados);
    }

    public boolean valida(Instant agora) {
        return revogadaEm == null && agora.isBefore(expiraEm);
    }

    public void revogar(Instant agora) {
        if (revogadaEm == null) revogadaEm = agora;
    }

    public void validarRefresh(String hash, Instant agora) {
        if (!valida(agora)) throw new CredenciaisInvalidasException();
        if (refreshUsados.contains(hash)) {
            revogar(agora);
            throw new CredenciaisInvalidasException();
        }
        if (!refreshHash.equals(hash)) throw new CredenciaisInvalidasException();
    }

    public void rotacionar(String novoHash) {
        refreshUsados.add(refreshHash);
        refreshHash = novoHash;
    }
}
