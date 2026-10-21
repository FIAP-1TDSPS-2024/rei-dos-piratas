package br.com.fiap.rei_dos_piratas.application.service;

import br.com.fiap.rei_dos_piratas.domain.entity.IdentidadeConta;

public interface UsuarioAtualService {
    IdentidadeConta identidade();
    java.util.UUID sessaoId();

    /** Indica se o usuario autenticado possui a permissao (ex.: "PEDIDO_WRITE"). Falso se nao houver usuario. */
    boolean possuiPermissao(String permissao);

    /** Identidade do usuario autenticado, ou vazio para chamadas sem usuario (ex.: webhooks e processos internos). */
    java.util.Optional<IdentidadeConta> identidadeSeAutenticado();
}
