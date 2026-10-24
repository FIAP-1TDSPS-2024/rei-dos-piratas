package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.CartaoClienteRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TokenizacaoCartaoRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.TokenizacaoCartaoResponseDto;
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
    private CobrancaAppClient apiCobranca;
    private UsuarioAtualService usuarioAtual;
    private CartaoService service;

    private final DadosCartaoCredito cartaoNovo = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

    @BeforeEach
    void setUp() {
        this.repository = mock(CartaoClienteRepository.class);
        this.apiCobranca = mock(CobrancaAppClient.class);
        this.usuarioAtual = mock(UsuarioAtualService.class);
        when(usuarioAtual.identidade()).thenReturn(new IdentidadeConta(TipoConta.CLIENTE, 1L));
        this.service = new CartaoServiceImpl(repository, apiCobranca, usuarioAtual);

        when(apiCobranca.tokenizarCartao(any())).thenReturn(new TokenizacaoCartaoResponseDto("1234", "VISA", "tok_abc123"));
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
    void cartaoNovo_DeveTokenizarComClienteDeCobrancaEDadosDoClienteComoTitular() {
        // Arrange
        Cliente cliente = criarCliente(1L);

        // Act
        service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(null, cartaoNovo, 1));

        // Assert
        ArgumentCaptor<TokenizacaoCartaoRequestDto> captor = ArgumentCaptor.forClass(TokenizacaoCartaoRequestDto.class);
        verify(apiCobranca).tokenizarCartao(captor.capture());
        TokenizacaoCartaoRequestDto request = captor.getValue();
        assertThat(request.customer()).isEqualTo("cus_000001");
        assertThat(request.creditCard().holderName()).isEqualTo("JOAO SILVA");
        assertThat(request.creditCard().number()).isEqualTo("4111111111111234");
        assertThat(request.creditCard().expiryMonth()).isEqualTo("12");
        assertThat(request.creditCard().expiryYear()).isEqualTo("2030");
        assertThat(request.creditCard().ccv()).isEqualTo("987");
        assertThat(request.creditCardHolderInfo().name()).isEqualTo("João Silva");
        assertThat(request.creditCardHolderInfo().email()).isEqualTo("joao@gmail.com");
        assertThat(request.creditCardHolderInfo().cpfCnpj()).isEqualTo("52998224725");
        assertThat(request.creditCardHolderInfo().mobilePhone()).isEqualTo("11992131234");
        assertThat(request.creditCardHolderInfo().postalCode()).isEqualTo("01001000");
        assertThat(request.creditCardHolderInfo().addressNumber()).isEqualTo("123");
    }

    @Test
    void cartaoNovo_DeveSalvarSomenteTokenUltimosDigitosEBandeira() {
        // Arrange
        Cliente cliente = criarCliente(1L);

        // Act
        CartaoCliente resultado = service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(null, cartaoNovo, 1));

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
        CartaoCliente resultado = service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1));

        // Assert
        assertThat(resultado).isSameAs(salvo);
        verify(apiCobranca, never()).tokenizarCartao(any());
        verify(repository, never()).create(any());
    }

    @Test
    void cartaoSalvo_DeOutroClienteDeveLancarNaoEncontrado() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        when(repository.findById(5L)).thenReturn(cartaoSalvo(5L, criarCliente(99L), true));

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(apiCobranca, never()).tokenizarCartao(any());
    }

    @Test
    void cartaoSalvo_InexistenteDeveLancarNaoEncontrado() {
        // Arrange: o repositório lança NoSuchElementException quando o id não existe
        Cliente cliente = criarCliente(1L);
        when(repository.findById(99L)).thenThrow(new java.util.NoSuchElementException());

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(99L, null, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cartaoSalvo_InativoDeveLancarRegraDeNegocio() {
        // Arrange
        Cliente cliente = criarCliente(1L);
        when(repository.findById(5L)).thenReturn(cartaoSalvo(5L, cliente, false));

        // Act & Assert
        assertThatThrownBy(() -> service.obterOuTokenizar(cliente, criarEndereco(cliente), new PagamentoCartao(5L, null, 1)))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(apiCobranca, never()).tokenizarCartao(any());
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
