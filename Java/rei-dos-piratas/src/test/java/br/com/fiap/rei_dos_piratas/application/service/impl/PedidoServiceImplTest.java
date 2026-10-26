package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CartaoService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.DadosEmpresaRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteCompanyDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.pedido.PedidoFreteResponseDto;
import br.com.fiap.rei_dos_piratas.infrastructure.external_interface.feign.CobrancaAppClient;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.EventoCobrancaDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PagamentoCobrancaRequestDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PagamentoCobrancaResponseDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PixQrCodeResponseDto;
import org.mockito.ArgumentCaptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PedidoServiceImplTest {

    private PedidoService pedidoService;
    private PedidoRepository pedidoRepository;
    private ProdutoRepository produtoRepository;
    private EnderecoService enderecoService;
    private br.com.fiap.rei_dos_piratas.application.service.ClienteService clienteService;
    private FreteService freteService;
    private DadosEmpresaRepository dadosEmpresaRepository;
    private UsuarioAtualService usuarioAtual;
    private CobrancaAppClient apiCobranca;
    private CartaoService cartaoService;

    @BeforeEach
    void setUp() {

        this.usuarioAtual = mock(UsuarioAtualService.class);
        autenticarComo(TipoConta.CLIENTE, 1L);

        this.pedidoRepository = mock(PedidoRepository.class);
        this.produtoRepository = mock(ProdutoRepository.class);
        this.freteService = mock(FreteService.class);
        this.dadosEmpresaRepository = mock(DadosEmpresaRepository.class);
        this.enderecoService = mock(EnderecoService.class);
        HmacUtil hmacUtil = mock(HmacUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        this.clienteService = mock(br.com.fiap.rei_dos_piratas.application.service.ClienteService.class);
        when(clienteService.obterOuCriarClienteCobranca(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        this.apiCobranca = mock(CobrancaAppClient.class);
        when(apiCobranca.criarNovaCobranca(any())).thenReturn(cobrancaCriada("pay_000001", new BigDecimal("219.20")));
        this.cartaoService = mock(CartaoService.class);
        RegraParcelamento regraParcelamento = new RegraParcelamento(new BigDecimal("100.00"), 3);
        this.pedidoService = new PedidoServiceImpl(pedidoRepository, produtoRepository, enderecoService, dadosEmpresaRepository, freteService, clienteService, apiCobranca, usuarioAtual,
                cartaoService, regraParcelamento);
    }

    private PagamentoCobrancaResponseDto cobrancaCriada(String id, BigDecimal valorLiquido) {
        return new PagamentoCobrancaResponseDto(null, id, null, null, null, null, null, valorLiquido,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    private void autenticarComo(TipoConta tipo, Long id, String... permissoes) {
        IdentidadeConta identidade = new IdentidadeConta(tipo, id);
        when(usuarioAtual.identidade()).thenReturn(identidade);
        when(usuarioAtual.identidadeSeAutenticado()).thenReturn(Optional.of(identidade));
        when(usuarioAtual.possuiPermissao(anyString())).thenReturn(false);
        for (String permissao : permissoes) {
            when(usuarioAtual.possuiPermissao(permissao)).thenReturn(true);
        }
    }

    private Pedido pedidoDoCliente(Long idPedido, Cliente cliente) {
        return new Pedido(idPedido, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.ZERO,
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, new ArrayList<>(), criarEndereco(cliente), 1L, null, null, null, null, null, null);
    }

    private Cliente criarCliente() {
        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());
        Perfil perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);

        return new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11992131234",
                carrinho);
    }

    private Endereco criarEndereco(Cliente cliente) {
        Estado estado = new Estado(
                1L,
                "São Paulo",
                "SP");

        Cidade cidade = new Cidade(
                1L,
                "São Paulo",
                estado);

        return new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                true,
                cidade,
                "Brasil",
                "BR",
                cliente);
    }

    private Funcionario criarFuncionario() {
        Perfil perfilAdmin = new Perfil(1L, "ADMIN", "Perfil de administrador", null);
        return new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                perfilAdmin,
                null,
                BigDecimal.valueOf(1000));
    }

    private List<FreteServiceDto> criarListaFretes(Long freteServiceId) {
        FreteCompanyDto company = new FreteCompanyDto(2L, "Jadlog",
                "https://sandbox.melhorenvio.com.br/images/shipping-companies/jadlog.png");
        FreteServiceDto frete = new FreteServiceDto(freteServiceId, ".Package",
                BigDecimal.valueOf(21.19), BigDecimal.valueOf(21.19), BigDecimal.ZERO, "R$", 6, company);
        return List.of(frete);
    }

    private Produto criarProduto(Long id, String nome, String autor, String categoria, int estoque, BigDecimal preco) {        return new Produto(
                id,
                nome,
                "Descrição do produto " + nome,
                autor,
                CategoriaEnum.valueOf(categoria),
                "http://imagem.com/" + nome + ".jpg",
                preco,
                preco,
                estoque,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                criarFuncionario());
    }

    @Test
    void findAll_DeveRetornarPaginaDePedidos() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "AVENTURA", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido1 = new Pedido(1L, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);
        Pedido pedido2 = new Pedido(2L, LocalDate.now(), null, null, null, BigDecimal.valueOf(300), BigDecimal.valueOf(21.19),
                StatusEnum.EM_TRANSITO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        List<Pedido> pedidos = List.of(pedido1, pedido2);
        Page<Pedido> page = new Page<>(1, 0, pedidos);

        // Mock do repository
        when(pedidoRepository.listAllByClient(0, 10, 1L)).thenReturn(page);

        // Act
        Page<Pedido> resultado = pedidoService.findAll(0, 10);

        // Assert
        verify(pedidoRepository, times(1)).listAllByClient(0, 10, 1L);
        assertThat(resultado.pageItems()).hasSize(2);
        assertThat(resultado.pageNumber()).isEqualTo(0);
        assertThat(resultado.numberOfPages()).isEqualTo(1);
    }

    @Test
    void findAll_FuncionarioComPermissaoDeEscritaDeveListarTodosOsPedidos() {
        autenticarComo(TipoConta.FUNCIONARIO, 5L, "PEDIDO_WRITE");
        Page<Pedido> todos = new Page<>(1, 0, List.of());
        when(pedidoRepository.listAll(0, 10)).thenReturn(todos);

        Page<Pedido> resultado = pedidoService.findAll(0, 10);

        assertThat(resultado).isSameAs(todos);
        verify(pedidoRepository, never()).listAllByClient(anyInt(), anyInt(), any());
    }

    @Test
    void findById_ClienteNaoDeveVerPedidoDeOutroCliente() {
        Cliente outroCliente = criarCliente();
        outroCliente.setId(99L);
        Pedido pedidoDeOutro = pedidoDoCliente(7L, outroCliente);
        when(pedidoRepository.findById(7L)).thenReturn(pedidoDeOutro);

        assertThatThrownBy(() -> pedidoService.findById(7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_FuncionarioDeveVerPedidoDeQualquerCliente() {
        autenticarComo(TipoConta.FUNCIONARIO, 5L);
        Cliente outroCliente = criarCliente();
        outroCliente.setId(99L);
        Pedido pedidoDeOutro = pedidoDoCliente(7L, outroCliente);
        when(pedidoRepository.findById(7L)).thenReturn(pedidoDeOutro);

        assertThat(pedidoService.findById(7L)).isSameAs(pedidoDeOutro);
    }

    @Test
    void findById_SemUsuarioAutenticadoDevePermitirAcesso_FluxosInternosEWebhooks() {
        when(usuarioAtual.identidadeSeAutenticado()).thenReturn(Optional.empty());
        Cliente outroCliente = criarCliente();
        outroCliente.setId(99L);
        Pedido pedido = pedidoDoCliente(7L, outroCliente);
        when(pedidoRepository.findById(7L)).thenReturn(pedido);

        assertThat(pedidoService.findById(7L)).isSameAs(pedido);
    }


    @Test
    void findById_DeveRetornarPedidoQuandoExistir() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        Pedido resultado = pedidoService.findById(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        assertThat(resultado).isEqualTo(pedido);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void findById_DeveLancarExcecaoQuandoNaoExistir() {
        // Arrange
        when(pedidoRepository.findById(999L)).thenThrow(new NoSuchElementException());

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Não foi possível encontrar um pedido com o id 999");

        verify(pedidoRepository, times(1)).findById(999L);
    }

    @Test
    void fazerPedido_DeveCriarPedidoEAtualizarEstoque() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        Pedido pedidoCriado = new Pedido(1L, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));
        pedidoCriado.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));

        when(pedidoRepository.create(any(Pedido.class))).thenReturn(pedidoCriado);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(criarListaFretes(1L));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("221.19");
        produto.setPreco(new BigDecimal("150.00"));
        assertThat(itens.getFirst().getPrecoUnitario()).isEqualByComparingTo("100.00");
        verify(produtoRepository, times(1)).update(produto);
        verify(pedidoRepository, times(1)).create(pedido);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
        assertThat(produto.getEstoque()).isEqualTo(8); // 10 - 2
    }

    @Test
    void fazerPedido_DeveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 4, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 5)); // Quantidade maior que estoque

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(500), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(criarListaFretes(1L));

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente")
                .hasMessageContaining("Produto Teste");

        verify(produtoRepository, never()).update(any(Produto.class));
        verify(pedidoRepository, never()).create(any(Pedido.class));
    }

    @Test
    void fazerPedido_DeveAtualizarEstoqueDeMultiplosProdutos() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto1 = criarProduto(1L, "Produto Teste 1", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto Teste 2", "Eichiro Oda", "AVENTURA", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto1, 2));
        itens.add(new ItemProdutoPedido(produto2, 1));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(350), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        Pedido pedidoCriado = new Pedido(2L, LocalDate.now(), null, null, null, BigDecimal.valueOf(350), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));
        pedidoCriado.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));

        when(pedidoRepository.create(any(Pedido.class))).thenReturn(pedidoCriado);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(criarListaFretes(1L));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        verify(produtoRepository, times(2)).update(any(Produto.class));
        verify(pedidoRepository, times(1)).create(pedido);
        assertThat(produto1.getEstoque()).isEqualTo(8);
        assertThat(produto2.getEstoque()).isEqualTo(9);
    }

    @Test
    void fazerPedido_PixDeveCriarCobrancaDoPedidoEGuardarIdEValorLiquido() {
        // Arrange
        Cliente cliente = criarCliente();
        cliente.setIdCobranca("cus_000001");
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));

        Pedido pedido = new Pedido(cliente, criarEndereco(cliente), itens, 1L, TipoPagamentoEnum.PIX);
        when(pedidoRepository.create(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(criarListaFretes(1L));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        ArgumentCaptor<PagamentoCobrancaRequestDto> cobranca = ArgumentCaptor.forClass(PagamentoCobrancaRequestDto.class);
        verify(apiCobranca).criarNovaCobranca(cobranca.capture());
        assertThat(cobranca.getValue().customer()).isEqualTo("cus_000001");
        assertThat(cobranca.getValue().billingType()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(cobranca.getValue().value()).isEqualByComparingTo("221.19");
        assertThat(cobranca.getValue().externalReference()).isEqualTo("10");
        assertThat(cobranca.getValue().creditCardToken()).isNull();
        assertThat(resultado.getPagamento().getIdCobranca()).isEqualTo("pay_000001");
        assertThat(resultado.getPagamento().getValorLiquido()).isEqualByComparingTo("219.20");
        assertThat(resultado.getPagamento().getValor()).isEqualByComparingTo("221.19");
        assertThat(resultado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void pagarPedido_DeveAtualizarStatusParaPreparandoEnvio() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        // Mock DadosEmpresa
        DadosEmpresa dadosEmpresa = new DadosEmpresa(
                1L,
                "Rei dos Piratas LTDA",
                "Rei dos Piratas",
                "12345678901234",
                "contato@reidospiratas.com.br",
                "1133334444",
                "reidospiratas.com.br",
                "SP",
                "00000000000191",
                "4721100");

        // Mock Endereco Empresa
        Endereco enderecoEmpresa = new Endereco(
                1L,
                123,
                "12345678",
                "Avenida Brasil",
                "Centro",
                true,
                new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")),
                "Brasil",
                "BR",
                null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dadosEmpresaRepository.get()).thenReturn(dadosEmpresa);
        when(enderecoService.getEnderecoEmpresa()).thenReturn(enderecoEmpresa);
        when(enderecoService.findById(1L)).thenReturn(criarEndereco(cliente));

        // Mock PedidoFreteResponseDto
        PedidoFreteResponseDto pedidoFreteResponse = new PedidoFreteResponseDto(
                UUID.randomUUID(),
                "PROTOCOL123456",
                3,
                1,
                BigDecimal.valueOf(21.19),
                BigDecimal.valueOf(21.19),
                BigDecimal.ZERO,
                5,
                6,
                "posted",
                false,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(freteService.criarPedidoFrete(any())).thenReturn(pedidoFreteResponse);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        pedidoService.pagarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(2)).update(pedido);  // Chamado em enriquecerPedidoPorFrete e em pagarPedido
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
    }


    @Test
    void cancelarPedido_DeveCancelarPedidoEDevolverEstoque() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 8, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.cancelarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).update(produto);
        verify(pedidoRepository, times(1)).update(pedido);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10); // 8 + 2
    }

    @Test
    void cancelarPedido_DeveDevolverEstoqueDeMultiplosProdutos() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto1 = criarProduto(1L, "Produto 1", "Jonas Oliveira", "ACAO", 8, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto 2", "Eichiro Oda", "AVENTURA", 4, BigDecimal.valueOf(150));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto1, 2));
        itens.add(new ItemProdutoPedido(produto2, 1));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.cancelarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(2)).update(any(Produto.class));
        verify(pedidoRepository, times(1)).update(pedido);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto1.getEstoque()).isEqualTo(10);
        assertThat(produto2.getEstoque()).isEqualTo(5);
    }

    @Test
    void cancelarPedido_DeveLancarExcecaoQuandoJaEstaCancelado() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(0),
                StatusEnum.CANCELADO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido já está cancelado");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).update(any(Produto.class));
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }

    @Test
    void cancelarPedido_DeveLancarExcecaoQuandoJaFoiEntregue() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(20),
                StatusEnum.ENTREGUE, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido já foi entregue");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).update(any(Produto.class));
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }

    // ---- Eventos de cobrança do cartão de crédito (Asaas) ----
    // Cartão: PAYMENT_CONFIRMED chega logo após a aprovação; PAYMENT_RECEIVED só quando o valor
    // é creditado (~30 dias depois), quando o pedido normalmente já foi enviado ou entregue.

    private Pedido pedidoDeCartaoAguardandoPagamento(Long id, int estoqueProduto, int quantidade) {
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", estoqueProduto, BigDecimal.valueOf(100));
        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, quantidade));
        Pedido pedido = new Pedido(id, LocalDate.now(), null, null, null, BigDecimal.valueOf(221.19), BigDecimal.valueOf(21.19),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens, criarEndereco(cliente), 1L, null, null, null, null, null, null);
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.CREDIT_CARD));
        pedido.getPagamento().setIdCobranca("pay_000001");
        return pedido;
    }

    private void prepararCriacaoDeFrete() {
        DadosEmpresa dadosEmpresa = new DadosEmpresa(1L, "Rei dos Piratas LTDA", "Rei dos Piratas", "12345678901234",
                "contato@reidospiratas.com.br", "1133334444", "reidospiratas.com.br", "SP", "00000000000191", "4721100");
        Endereco enderecoEmpresa = new Endereco(1L, 123, "12345678", "Avenida Brasil", "Centro", true,
                new Cidade(1L, "São Paulo", new Estado(1L, "São Paulo", "SP")), "Brasil", "BR", null);
        when(dadosEmpresaRepository.get()).thenReturn(dadosEmpresa);
        when(enderecoService.getEnderecoEmpresa()).thenReturn(enderecoEmpresa);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(freteService.criarPedidoFrete(any())).thenReturn(new PedidoFreteResponseDto(UUID.randomUUID(), "PROTOCOL123456",
                3, 1, BigDecimal.valueOf(21.19), BigDecimal.valueOf(21.19), BigDecimal.ZERO, 5, 6, "pending", false,
                LocalDateTime.now(), LocalDateTime.now()));
    }

    private EventoCobrancaDto eventoCobranca(String evento, Long pedidoId) {
        return new EventoCobrancaDto("evt_000001", evento, LocalDateTime.now(),
                new EventoCobrancaDto.Payment("pay_000001", null, pedidoId.toString()), null);
    }

    @Test
    void processarEventoCobranca_PagamentoConfirmadoDoCartaoDeveLiberarPedidoParaEnvio() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_CONFIRMED", 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
        assertThat(pedido.getPedidoFrete()).isNotNull();
    }

    @Test
    void processarEventoCobranca_PagamentoRecebidoDePedidoJaLiberadoNaoDeveFalharNemCriarOutroFrete() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        pedido.setStatus(StatusEnum.EM_TRANSITO);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_RECEIVED", 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
        verify(freteService, never()).criarPedidoFrete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PAYMENT_CREDIT_CARD_CAPTURE_REFUSED", "PAYMENT_REPROVED_BY_RISK_ANALYSIS"})
    void processarEventoCobranca_CartaoRecusadoDeveCancelarPedidoEDevolverEstoque(String evento) {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 8, 2);
        Produto produto = pedido.getProdutosAdicionados().getFirst().getProduto();
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(evento, 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10); // 8 + 2
        verify(freteService, never()).criarPedidoFrete(any());
    }

    // ---- Criação do pedido com cartão de crédito ----
    // Total dos cenários: 2 x R$ 100,00 + frete R$ 21,19 = R$ 221,19

    private final DadosCartaoCredito cartaoNovo = new DadosCartaoCredito("JOAO SILVA", "4111111111111234", "12", "2030", "987");

    private Pedido novoPedido(Produto produto, TipoPagamentoEnum tipoPagamento) {
        Cliente cliente = criarCliente();
        cliente.setIdCobranca("cus_000001");
        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));
        return new Pedido(cliente, criarEndereco(cliente), itens, 1L, tipoPagamento);
    }

    private void prepararCriacaoDoPedido(Pedido pedido) {
        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(criarListaFretes(1L));
        when(pedidoRepository.create(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });
        when(pedidoRepository.findById(10L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /** Pagamento é salvo junto com o pedido (agregado), então o efeito observável é o último update. */
    private Pedido ultimoPedidoGravado() {
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository, atLeastOnce()).update(captor.capture());
        return captor.getAllValues().getLast();
    }

    private CartaoCliente prepararCartaoTokenizado() {
        CartaoCliente cartao = new CartaoCliente(50L, null, "tok_abc123", "1234", "VISA", true, LocalDateTime.now());
        when(cartaoService.obterOuTokenizar(any(), any(), any())).thenReturn(cartao);
        return cartao;
    }

    private Produto produtoComEstoque(int estoque) {
        return criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", estoque, BigDecimal.valueOf(100));
    }

    @Test
    void fazerPedido_CartaoSemDadosDoCartaoDeveLancarRegraDeNegocioSemGravarPedido() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, null))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(pedidoRepository, never()).create(any());
        verify(apiCobranca, never()).criarNovaCobranca(any());
    }

    @Test
    void fazerPedido_CartaoComParcelamentoInvalidoDeveLancarRegraDeNegocioSemGravarPedido() {
        // Arrange
        Produto produto = produtoComEstoque(10);
        Pedido pedido = novoPedido(produto, TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 4, "200.1.2.3")))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(pedidoRepository, never()).create(any());
        verify(apiCobranca, never()).criarNovaCobranca(any());
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void fazerPedido_CartaoDeveEnviarOIpDoCompradorNaCobranca() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        ArgumentCaptor<PagamentoCobrancaRequestDto> captor = ArgumentCaptor.forClass(PagamentoCobrancaRequestDto.class);
        verify(apiCobranca).criarNovaCobranca(captor.capture());
        assertThat(captor.getValue().remoteIp()).isEqualTo("200.1.2.3");
    }

    @Test
    void fazerPedido_CartaoAVistaDeveCriarCobrancaComTokenDoCartao() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        ArgumentCaptor<PagamentoCobrancaRequestDto> captor = ArgumentCaptor.forClass(PagamentoCobrancaRequestDto.class);
        verify(apiCobranca).criarNovaCobranca(captor.capture());
        PagamentoCobrancaRequestDto request = captor.getValue();
        assertThat(request.customer()).isEqualTo("cus_000001");
        assertThat(request.billingType()).isEqualTo(TipoPagamentoEnum.CREDIT_CARD);
        assertThat(request.creditCardToken()).isEqualTo("tok_abc123");
        assertThat(request.value()).isEqualByComparingTo("221.19");
        assertThat(request.installmentCount()).isNull();
        assertThat(request.totalValue()).isNull();
        assertThat(request.externalReference()).isEqualTo("10");
    }

    @Test
    void fazerPedido_CartaoParceladoDeveEnviarQuantidadeDeParcelasEValorTotal() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 3, "200.1.2.3"));

        // Assert
        ArgumentCaptor<PagamentoCobrancaRequestDto> captor = ArgumentCaptor.forClass(PagamentoCobrancaRequestDto.class);
        verify(apiCobranca).criarNovaCobranca(captor.capture());
        assertThat(captor.getValue().installmentCount()).isEqualTo(3);
        assertThat(captor.getValue().totalValue()).isEqualByComparingTo("221.19");
        assertThat(captor.getValue().value()).isNull();
    }

    @Test
    void fazerPedido_CartaoAprovadoDeveGravarPagamentoConfirmado() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        CartaoCliente cartao = prepararCartaoTokenizado();

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 2, "200.1.2.3"));

        // Assert
        Pagamento pagamento = ultimoPedidoGravado().getPagamento();
        assertThat(pagamento).isNotNull();
        assertThat(pagamento.getTipo()).isEqualTo(TipoPagamentoEnum.CREDIT_CARD);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.CONFIRMADO);
        assertThat(pagamento.getIdCobranca()).isEqualTo("pay_000001");
        assertThat(pagamento.getValor()).isEqualByComparingTo("221.19");
        assertThat(pagamento.getValorLiquido()).isEqualByComparingTo("219.20");
        assertThat(pagamento.getParcelas()).isEqualTo(2);
        assertThat(pagamento.getCartao()).isSameAs(cartao);
        assertThat(pagamento.getDataCriacao()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void fazerPedido_CartaoRecusadoDeveLancarErroGravarPagamentoRecusadoCancelarPedidoEDevolverEstoque() {
        // Arrange
        Produto produto = produtoComEstoque(10);
        Pedido pedido = novoPedido(produto, TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        when(apiCobranca.criarNovaCobranca(any())).thenThrow(new PagamentoRecusadoException("Transação não autorizada"));

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3")))
                .isInstanceOf(PagamentoRecusadoException.class)
                .hasMessageContaining("Transação não autorizada");

        Pedido gravado = ultimoPedidoGravado();
        assertThat(gravado.getPagamento()).isNotNull();
        assertThat(gravado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECUSADO);
        assertThat(gravado.getPagamento().getMotivoRecusa()).contains("Transação não autorizada");
        assertThat(gravado.getPagamento().getIdCobranca()).isNull();
        assertThat(gravado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10); // baixado para 8 e devolvido
    }

    @Test
    void fazerPedido_PixSemPagamentoCartaoDeveCriarCobrancaPixSemTokenizar() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.PIX);
        prepararCriacaoDoPedido(pedido);

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido, null);

        // Assert
        ArgumentCaptor<PagamentoCobrancaRequestDto> captor = ArgumentCaptor.forClass(PagamentoCobrancaRequestDto.class);
        verify(apiCobranca).criarNovaCobranca(captor.capture());
        assertThat(captor.getValue().billingType()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(captor.getValue().creditCardToken()).isNull();
        verify(cartaoService, never()).obterOuTokenizar(any(), any(), any());
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    // ---- QR Code PIX ----

    @Test
    void obterQrCodeCobrancaPix_DeveConsultarPelaCobrancaDoPagamento() {
        // Arrange
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));
        pedido.getPagamento().setIdCobranca("pay_qr");
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        PixQrCodeResponseDto qrCode = new PixQrCodeResponseDto(true, "base64", "copia-e-cola", LocalDateTime.now(), null);
        when(apiCobranca.obterQrCodePix("pay_qr")).thenReturn(qrCode);

        // Act
        PixQrCodeResponseDto resultado = pedidoService.obterQrCodeCobrancaPix(1L);

        // Assert
        assertThat(resultado).isSameAs(qrCode);
    }

    @Test
    void obterQrCodeCobrancaPix_SemCobrancaNoPagamentoDeveLancarRegraDeNegocio() {
        // Arrange
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obterQrCodeCobrancaPix(1L))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(apiCobranca, never()).obterQrCodePix(any());
    }

    // ---- Status do pagamento nos eventos do Asaas ----

    private EventoCobrancaDto eventoCobranca(String evento, Long pedidoId, String motivoRecusa) {
        return new EventoCobrancaDto("evt_000002", evento, LocalDateTime.now(),
                new EventoCobrancaDto.Payment("pay_000001", null, pedidoId.toString()),
                new EventoCobrancaDto.AdditionalInfo(motivoRecusa));
    }

    @Test
    void processarEventoCobranca_PagamentoConfirmadoDeveMarcarPagamentoComoConfirmado() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_CONFIRMED", 1L));

        // Assert
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.CONFIRMADO);
        assertThat(pedido.getPagamento().getDataConfirmacao()).isNotNull();
    }

    @Test
    void processarEventoCobranca_PagamentoRecebidoDeveMarcarPagamentoComoRecebidoMesmoComPedidoJaEnviado() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        pedido.setStatus(StatusEnum.EM_TRANSITO);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_RECEIVED", 1L));

        // Assert
        assertThat(ultimoPedidoGravado().getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
    }

    @Test
    void processarEventoCobranca_CartaoRecusadoDeveMarcarPagamentoRecusadoComOMotivo() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 8, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_CREDIT_CARD_CAPTURE_REFUSED", 1L, "Saldo insuficiente"));

        // Assert
        Pagamento pagamento = ultimoPedidoGravado().getPagamento();
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.RECUSADO);
        assertThat(pagamento.getMotivoRecusa()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void processarEventoCobranca_RecusaDePedidoJaCanceladoNaoDeveFalharNemDevolverEstoqueDeNovo() {
        // Arrange: recusa síncrona já cancelou o pedido; o webhook chega depois
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        pedido.setStatus(StatusEnum.CANCELADO);
        Produto produto = pedido.getProdutosAdicionados().getFirst().getProduto();
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca("PAYMENT_CREDIT_CARD_CAPTURE_REFUSED", 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10);
        verify(produtoRepository, never()).update(any());
    }

    @Test
    void fazerPedido_CartaoEmAnaliseDeRiscoDeveManterPagamentoPendente() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        when(apiCobranca.criarNovaCobranca(any())).thenReturn(new PagamentoCobrancaResponseDto(null, "pay_000001", null,
                null, null, null, null, new BigDecimal("219.20"), null, null, null, null, null,
                br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.Enum.AsaasPaymentStatus.AWAITING_RISK_ANALYSIS,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null));

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        Pagamento pagamento = ultimoPedidoGravado().getPagamento();
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(pagamento.getIdCobranca()).isEqualTo("pay_000001");
    }

    // ---- Data de entrega no fuso da loja ----

    @Test
    void rastreioPedidoWebhook_EntregaDepoisDas21hDeveUsarADataDoBrasil() {
        // Arrange: 23:30 em Brasília = 02:30 do dia seguinte em UTC (como o Jackson entrega o OffsetDateTime)
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setStatus(StatusEnum.EM_TRANSITO);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        java.time.OffsetDateTime entregueEmUtc = java.time.OffsetDateTime.parse("2026-10-24T02:30:00Z");
        var data = new br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioDataDto(
                UUID.randomUUID().toString(), "PROTOCOLO", "delivered", "TRACK123", null,
                null, null, null, null, entregueEmUtc, null, null, "https://example.com/tracking");

        // Act
        pedidoService.rastreioPedidoWebhook(pedido,
                new br.com.fiap.rei_dos_piratas.interfaces.dto.frete.webhook.RastreioWebhookDto("order.delivered", data));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.ENTREGUE);
        assertThat(pedido.getDataEntrega()).isEqualTo(LocalDate.of(2026, 10, 23));
    }
}
