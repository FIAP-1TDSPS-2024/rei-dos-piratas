package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Conta;
import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;
import java.time.Instant;
import java.util.UUID;

public interface AutenticacaoService {
    Resultado login(String email, String senha);
    Resultado cadastrar(Cliente cliente);

    Resultado renovar(String refreshToken);
    void logout(UUID sessaoId);
    boolean sessaoValida(UUID sessaoId, IdentidadeConta identidade);

    record Resultado(String token, Conta conta, String refreshToken, Instant refreshExpiraEm) {}
}
