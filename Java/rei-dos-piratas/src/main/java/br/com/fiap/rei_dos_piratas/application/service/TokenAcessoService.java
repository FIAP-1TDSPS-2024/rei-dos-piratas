package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;
import br.com.fiap.rei_dos_piratas.domain.entity.Sessao;

public interface TokenAcessoService {
    String emitir(Conta conta, Sessao sessao);
}
