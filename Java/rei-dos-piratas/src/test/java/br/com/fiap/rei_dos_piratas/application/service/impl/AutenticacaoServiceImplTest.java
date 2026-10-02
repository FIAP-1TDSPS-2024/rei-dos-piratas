package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.Conta;

import br.com.fiap.rei_dos_piratas.domain.repository.ContaRepository;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.SenhaService;
import br.com.fiap.rei_dos_piratas.application.service.TokenAcessoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoServiceImplTest {
    private final ContaRepository contas = mock(ContaRepository.class);
    private final SenhaService senhas = mock(SenhaService.class);
    private final TokenAcessoService tokens = mock(TokenAcessoService.class);
    private final ClienteService clientes = mock(ClienteService.class);
    private final AutenticacaoServiceImpl service = new AutenticacaoServiceImpl(contas, senhas, tokens, clientes);

    @Test
    void funcionarioSemPermissaoDeProdutoContinuaSendoFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setUsuarioAtivo(true);
        funcionario.setSenha("hash");
        funcionario.setPerfil(new Perfil(1L, "USER", "", List.of()));
        Conta conta = new Conta(new IdentidadeConta(TipoConta.FUNCIONARIO, 1L), funcionario);
        when(contas.findByEmail("staff@example.com")).thenReturn(Optional.of(conta));
        when(senhas.matches("senha", "hash")).thenReturn(true);
        when(tokens.emitir(conta)).thenReturn("jwt");
        var resultado = service.login(" STAFF@Example.com ", "senha");
        assertThat(resultado.conta().identidade().tipo()).isEqualTo(TipoConta.FUNCIONARIO);
        assertThat(resultado.token()).isEqualTo("jwt");
        verifyNoInteractions(clientes);
    }

    @Test
    void contaInativaOuSenhaIncorretaNaoEmiteToken() {
        Cliente cliente = new Cliente();
        cliente.setSenha("hash");
        Conta conta = new Conta(new IdentidadeConta(TipoConta.CLIENTE, 1L), cliente);
        when(contas.findByEmail("c@example.com")).thenReturn(Optional.of(conta));
        when(senhas.matches("senha", "hash")).thenReturn(true);
        assertThatThrownBy(() -> service.login("c@example.com", "senha")).isInstanceOf(CredenciaisInvalidasException.class);
        cliente.setUsuarioAtivo(true);
        assertThatThrownBy(() -> service.login("c@example.com", "errada")).isInstanceOf(CredenciaisInvalidasException.class);
        assertThatThrownBy(() -> service.login("ausente@example.com", "senha")).isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(tokens);
    }

    @Test
    void cadastroUsaContaCriadaSemAutenticarNovamente() {
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setUsuarioAtivo(true);
        when(clientes.create(cliente)).thenReturn(cliente);
        when(tokens.emitir(any())).thenReturn("jwt");
        var resultado = service.cadastrar(cliente);
        assertThat(resultado.conta().identidade()).isEqualTo(new IdentidadeConta(TipoConta.CLIENTE, 2L));
        verifyNoInteractions(contas, senhas);
    }
}
