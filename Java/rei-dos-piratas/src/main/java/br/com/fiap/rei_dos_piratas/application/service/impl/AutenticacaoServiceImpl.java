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
import org.springframework.transaction.annotation.Transactional;

public class AutenticacaoServiceImpl implements AutenticacaoService {
    private final ContaRepository contas;
    private final SenhaService senhas;
    private final TokenAcessoService tokens;
    private final ClienteService clientes;

    public AutenticacaoServiceImpl(ContaRepository contas, SenhaService senhas, TokenAcessoService tokens, ClienteService clientes) {
        this.contas = contas;
        this.senhas = senhas;
        this.tokens = tokens;
        this.clientes = clientes;
    }

    @Override
    public Resultado login(String email, String senha) {
        Conta conta = contas.findByEmail(Usuario.normalizarEmail(email))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!senhas.matches(senha, conta.usuario().getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        conta.usuario().validarAcesso();
        return new Resultado(tokens.emitir(conta), conta);
    }

    @Override
    @Transactional
    public Resultado cadastrar(Cliente cliente) {
        Cliente criado = clientes.create(cliente);
        criado.validarAcesso();
        Conta conta = new Conta(new IdentidadeConta(TipoConta.CLIENTE, criado.getId()), criado);
        return new Resultado(tokens.emitir(conta), conta);
    }
}
