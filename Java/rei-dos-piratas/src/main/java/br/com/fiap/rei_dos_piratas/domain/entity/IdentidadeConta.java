package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;

public record IdentidadeConta(TipoConta tipo, Long id) {
    public IdentidadeConta {
        if (tipo == null || id == null) {
            throw new IllegalArgumentException("Identidade de conta invalida");
        }
    }

    public String subject() {
        return tipo.name() + ":" + id;
    }
}
