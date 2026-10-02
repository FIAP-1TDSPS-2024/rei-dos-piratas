package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

public interface AutenticacaoService {
    Resultado login(String email, String senha);
    Resultado cadastrar(Cliente cliente);

    record Resultado(String token, Conta conta) {}
}
