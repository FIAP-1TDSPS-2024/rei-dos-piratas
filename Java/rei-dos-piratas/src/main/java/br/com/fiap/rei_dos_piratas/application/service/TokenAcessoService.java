package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

public interface TokenAcessoService {
    String emitir(Conta conta);
}
