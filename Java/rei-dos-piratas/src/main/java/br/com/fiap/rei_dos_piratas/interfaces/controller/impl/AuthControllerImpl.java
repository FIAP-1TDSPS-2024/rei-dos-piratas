package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.AutenticacaoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.Cliente;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.ClienteDtoMapper;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.usuarios.FuncionarioDtoMapper;
import br.com.fiap.rei_dos_piratas.interfaces.controller.AuthController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.usuarios.*;

public class AuthControllerImpl implements AuthController {
    private final AutenticacaoService autenticacao;
    private final br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService usuarioAtual;

    public AuthControllerImpl(AutenticacaoService autenticacao,
                              br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService usuarioAtual) {
        this.autenticacao = autenticacao;
        this.usuarioAtual = usuarioAtual;
    }

    public AuthResponse login(LoginRequest request) {
        return toDto(autenticacao.login(request.email(), request.password()));
    }

    public AuthResponse cadastrar(ClienteInDto request) {
        return toDto(autenticacao.cadastrar(ClienteDtoMapper.toEntity(request)));
    }

    private AuthResponse toDto(AutenticacaoService.Resultado resultado) {
        var conta = resultado.conta();
        var cliente = conta.identidade().tipo() == TipoConta.CLIENTE
                ? ClienteDtoMapper.toDto((Cliente) conta.usuario()) : null;
        var funcionario = conta.identidade().tipo() == TipoConta.FUNCIONARIO
                ? FuncionarioDtoMapper.toDto((Funcionario) conta.usuario()) : null;
        return new AuthResponse(
                resultado.token(),
                cliente,
                funcionario,
                conta.usuario()
                        .getPermissoes()
                        .stream()
                        .map(nome -> "ROLE_" + nome)
                        .toList(), resultado.refreshToken(), resultado.refreshExpiraEm());
    }

    public AuthResponse renovar(String refreshToken) {
        return toDto(autenticacao.renovar(refreshToken));
    }

    public void logout() { autenticacao.logout(usuarioAtual.sessaoId()); }
}
