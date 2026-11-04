package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.domain.gateway.CartaoTokenizado;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartaoServiceImplTest {

    private CartaoClienteRepository repository;
    private GatewayPagamento gatewayPagamento;
    private UsuarioAtualService usuarioAtual;
    private CartaoService service;

    private final DadosCartaoCredito cartaoNovo = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

    @BeforeEach
    void setUp() {
        this.repository = mock(CartaoClienteRepository.class);
        this.gatewayPagamento = mock(GatewayPagamento.class);
        this.usuarioAtual = mock(UsuarioAtualService.class);
        when(usuarioAtual.identidade()).thenReturn(new IdentidadeConta(TipoConta.CLIENTE, 1L));
        this.service = new CartaoServiceImpl(repository, gatewayPagamento, usuarioAtual);

        when(gatewayPagamento.tokenizarCartao(any(), any(), any(), any())).thenReturn(new CartaoTokenizado("tok_abc123", "1234", "VISA"));
        when(repository.create(any(CartaoCliente.class))).thenAnswer(invocation -> {
            CartaoCliente salvo = invocation.getArgument(0);
            salvo.setId(50L);
            return salvo;
        });
    }

    private Cliente criarCliente(Long id) {
        Cliente cliente = new Cliente(id, "joaosilva", "João Silva", "joao@gmail.com", "SenhaSegura123", true,
                LocalDate.now(), new Perfil(1L, "CLIENT", "Perfil de cliente", null), LocalDate.of(1990, 5, 15),
                SexoEnum.M, "52998224725", "11992131234", new Carrinho(1L, new ArrayList<>()));
        cliente.setIdCobranca("cus_000001");
        return cliente;
    }

    private Endereco criarEndereco(Cliente cliente) {
        return new Endereco(1L, 123, "01001000", "Avenida Paulista", "Bela Vista", true,
                new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", cliente);
    }

    private CartaoCliente cartaoSalvo(Long id, Cliente dono, boolean ativo) {
        return new CartaoCliente(id, dono, "tok_salvo", "4321", "MASTERCARD", ativo, LocalDateTime.now());
    }

    @Test
    void cartaoNovo_DeveEnviarOIpDoCompradorNaTokenizacao() {
        // Arrange
        Cliente cliente = criarCliente(1L);

        // Act
        service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        verify(gatewayPagamento).tokenizarCartao(any(), any(), any(), eq("200.1.2.3"));
    }

    @Test
    void cartaoNovo_DeveTokenizarComOClienteOEnderecoEOsDadosDoCartao() {
        // Arrange (o formato enviado ao Asaas é coberto em GatewayPagamentoAsaasTest)
        Cliente cliente = criarCliente(1L);
        Endereco endereco = criarEndereco(cliente);

        // Act
        service.obterOuTokenizar(cliente, endereco, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        verify(gatewayPagamento).tokenizarCartao(same(cliente), same(endereco), eq(cartaoNovo), any());
    }

    @Test
    void cartaoNovo_DeveSalvarSomenteTokenUltimosDigitosEBandeira() {
        // Arrange
        Cliente cliente = criarCliente(1L);

        // Act
        CartaoCliente resultado = service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        ArgumentCaptor<CartaoCliente> captor = ArgumentCaptor.forClass(CartaoCliente.class);
        verify(repository).create(captor.capture());
        CartaoCliente salvo = captor.getValue();
        assertThat(salvo.getCliente()).isSameAs(cliente);
        assertThat(salvo.getToken()).isEqualTo("tok_abc123");
        assertThat(salvo.getUltimosDigitos()).isEqualTo("1234");
        assertThat(salvo.getBandeira()).isEqualTo("VISA");
        assertThat(salvo.getAtivo()).isTrue();
        assertThat(salvo.getDataCadastro()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(50L);
    }

    @Test
    void cartaoSalvo_DeveUsarTokenGravadoSemChamarOAsaas() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        CartaoCliente salvo = cartaoSalvo(5L, cliente, true);
        when(repository.findById(5L)).thenReturn(salvo);

        // Act
        CartaoCliente resultado = service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1, "200.1.2.3"));

        // Assert
        assertThat(resultado).isSameAs(salvo);
        verify(gatewayPagamento, never()).tokenizarCartao(any(), any(), any(), any());
        verify(repository, never()).create(any());
    }

    @Test
    void cartaoSalvo_DeOutroClienteDeveLancarNaoEncontrado() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        when(repository.findById(5L)).thenReturn(cartaoSalvo(5L, criarCliente(99L), true));

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1, "200.1.2.3")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(gatewayPagamento, never()).tokenizarCartao(any(), any(), any(), any());
    }

    @Test
    void cartaoSalvo_InexistenteDeveLancarNaoEncontrado() {
        // Arrange: o repositório lança NoSuchElementException quando o id não existe
        Cliente cliente = criarCliente(1L);
        when(repository.findById(99L)).thenThrow(new java.util.NoSuchElementException());

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(99L, null, 1, "200.1.2.3")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cartaoSalvo_InativoDeveLancarRegraDeNegocio() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        when(repository.findById(5L)).thenReturn(cartaoSalvo(5L, cliente, false));

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1, "200.1.2.3")))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(gatewayPagamento, never()).tokenizarCartao(any(), any(), any(), any());
    }

    // ---- Consulta dos cartões salvos ----

    @Test
    void findAllByClienteId_DeveRetornarOsCartoesDoRepositorio() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        List<CartaoCliente> cartoes = List.of(cartaoSalvo(5L, cliente, true));
        when(repository.findAllByClienteId(1L)).thenReturn(cartoes);

        // Act
        List<CartaoCliente> resultado = service.findAllByClienteId(1L);

        // Assert
        assertThat(resultado).isEqualTo(cartoes);
    }

    @Test
    void listarCartoesDoClienteAtual_DeveRetornarSomenteOsCartoesAtivosDoClienteLogado() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        CartaoCliente ativo = cartaoSalvo(5L, cliente, true);
        CartaoCliente removido = cartaoSalvo(6L, cliente, false);
        when(repository.findAllByClienteId(1L)).thenReturn(List.of(ativo, removido));

        // Act
        List<CartaoCliente> resultado = service.listarCartoesDoClienteAtual();

        // Assert
        assertThat(resultado).containsExactly(ativo);
    }

    @Test
    void listarCartoesDoClienteAtual_ClienteSemCartoesDeveRetornarListaVazia() {
        when(repository.findAllByClienteId(1L)).thenReturn(List.of());

        assertThat(service.listarCartoesDoClienteAtual()).isEmpty();
    }

    @Test
    void listarCartoesDoClienteAtual_FuncionarioNaoTemCartoesSalvos() {
        // Arrange: ids de cliente e funcionário podem coincidir (tabelas diferentes)
        when(usuarioAtual.identidade()).thenReturn(new IdentidadeConta(TipoConta.FUNCIONARIO, 1L));

        // Act & Assert
        assertThatThrownBy(() -> service.listarCartoesDoClienteAtual())
                .isInstanceOf(RegraDeNegocioException.class);
        verify(repository, never()).findAllByClienteId(any());
    }
}
