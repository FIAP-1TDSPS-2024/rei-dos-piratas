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
import br.com.fiap.rei_dos_piratas.domain.gateway.*;
import br.com.fiap.rei_dos_piratas.infrastructure.security.HmacUtil;
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
import java.util.Map;
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
    private GatewayFrete gatewayFrete;
    private DadosEmpresaRepository dadosEmpresaRepository;
    private UsuarioAtualService usuarioAtual;
    private GatewayPagamento gatewayPagamento;
    private CartaoService cartaoService;
    private ExecutorTransacionalFalso executor;

    /** Executa na hora e registra se o trecho está dentro de uma transação. */
    static class ExecutorTransacionalFalso implements ExecutorTransacional {
        boolean dentroDeTransacao;
        int transacoes;

        @Override
        public <T> T emTransacao(java.util.function.Supplier<T> operacao) {
            transacoes++;
            dentroDeTransacao = true;
            try {
                return operacao.get();
            } finally {
                dentroDeTransacao = false;
            }
        }
    }

    @BeforeEach
    void setUp() {

        this.usuarioAtual = mock(UsuarioAtualService.class);
        autenticarComo(TipoConta.CLIENTE, 1L);

        this.pedidoRepository = mock(PedidoRepository.class);
        this.produtoRepository = mock(ProdutoRepository.class);
        this.freteService = mock(FreteService.class);
        this.gatewayFrete = mock(GatewayFrete.class);
        this.dadosEmpresaRepository = mock(DadosEmpresaRepository.class);
        this.enderecoService = mock(EnderecoService.class);
        HmacUtil hmacUtil = mock(HmacUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        this.clienteService = mock(br.com.fiap.rei_dos_piratas.application.service.ClienteService.class);
        when(clienteService.obterOuCriarClienteCobranca(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        this.gatewayPagamento = mock(GatewayPagamento.class);
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaCriada("pay_000001", new BigDecimal("219.20")));
        this.cartaoService = mock(CartaoService.class);
        this.executor = new ExecutorTransacionalFalso();
        this.pedidoService = servicoComBoletoVencendoEm(3);
    }

    private PedidoService servicoComBoletoVencendoEm(int diasVencimento) {
        RegraParcelamento regraParcelamento = new RegraParcelamento(new BigDecimal("100.00"), 3);
        return new PedidoServiceImpl(pedidoRepository, produtoRepository, enderecoService, dadosEmpresaRepository, freteService, gatewayFrete, clienteService, gatewayPagamento, usuarioAtual,
                cartaoService, regraParcelamento, new RegraBoleto(diasVencimento), executor);
    }

    /** Cobrança de boleto criada: id, valor líquido e URL do PDF. */
    private CobrancaCriada cobrancaDeBoleto(String id, String urlBoleto) {
        return new CobrancaCriada(id, new BigDecimal("218.20"), false, urlBoleto);
    }

    /** Cobrança criada ainda pendente (PIX/boleto ou cartão em análise de risco). */
    private CobrancaCriada cobrancaCriada(String id, BigDecimal valorLiquido) {
        return new CobrancaCriada(id, valorLiquido, false, null);
    }

    /** Cartão aprovado na própria criação da cobrança. */
    private CobrancaCriada cobrancaAprovada(String id, BigDecimal valorLiquido) {
        return new CobrancaCriada(id, valorLiquido, true, null);
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

    private List<OpcaoFrete> criarListaFretes(Long freteServiceId) {
        Transportadora company = new Transportadora(2L, "Jadlog",
                "https://sandbox.melhorenvio.com.br/images/shipping-companies/jadlog.png");
        OpcaoFrete frete = new OpcaoFrete(freteServiceId, ".Package",
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
        ArgumentCaptor<SolicitacaoCobranca> cobranca = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(cobranca.capture());
        assertThat(cobranca.getValue().idClienteExterno()).isEqualTo("cus_000001");
        assertThat(cobranca.getValue().tipo()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(cobranca.getValue().valor()).isEqualByComparingTo("221.19");
        assertThat(cobranca.getValue().referenciaPedido()).isEqualTo("10");
        assertThat(cobranca.getValue().tokenCartao()).isNull();
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

        // Envio criado no fornecedor
        EnvioCriado pedidoFreteResponse = new EnvioCriado(UUID.randomUUID(), "PROTOCOL123456", BigDecimal.valueOf(21.19), 6);

        when(gatewayFrete.criarEnvio(any())).thenReturn(pedidoFreteResponse);
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
        when(gatewayFrete.criarEnvio(any())).thenReturn(new EnvioCriado(UUID.randomUUID(), "PROTOCOL123456", BigDecimal.valueOf(21.19), 6));
    }

    private EventoPagamento eventoCobranca(TipoEventoPagamento tipo, Long pedidoId) {
        return eventoCobranca(tipo, pedidoId, null);
    }

    @Test
    void processarEventoCobranca_PagamentoConfirmadoDoCartaoDeveLiberarPedidoParaEnvio() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.CONFIRMADA, 1L));

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
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECEBIDA, 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
        verify(gatewayFrete, never()).criarEnvio(any());
    }

    @Test
    void processarEventoCobranca_CartaoRecusadoDeveCancelarPedidoEDevolverEstoque() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 8, 2);
        Produto produto = pedido.getProdutosAdicionados().getFirst().getProduto();
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECUSADA, 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10); // 8 + 2
        verify(gatewayFrete, never()).criarEnvio(any());
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
        verify(gatewayPagamento, never()).criarCobranca(any());
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
        verify(gatewayPagamento, never()).criarCobranca(any());
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
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        assertThat(captor.getValue().ipRemoto()).isEqualTo("200.1.2.3");
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
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        SolicitacaoCobranca request = captor.getValue();
        assertThat(request.idClienteExterno()).isEqualTo("cus_000001");
        assertThat(request.tipo()).isEqualTo(TipoPagamentoEnum.CREDIT_CARD);
        assertThat(request.tokenCartao()).isEqualTo("tok_abc123");
        assertThat(request.valor()).isEqualByComparingTo("221.19");
        assertThat(request.parcelas()).isEqualTo(1);
        assertThat(request.referenciaPedido()).isEqualTo("10");
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
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        assertThat(captor.getValue().parcelas()).isEqualTo(3);
        assertThat(captor.getValue().valor()).isEqualByComparingTo("221.19");
    }

    @Test
    void fazerPedido_CartaoAprovadoDeveGravarPagamentoConfirmado() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        CartaoCliente cartao = prepararCartaoTokenizado();
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaAprovada("pay_000001", new BigDecimal("219.20")));

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
        when(gatewayPagamento.criarCobranca(any())).thenThrow(new PagamentoRecusadoException("Transação não autorizada"));

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
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(captor.getValue().tokenCartao()).isNull();
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
        QrCodePix qrCode = new QrCodePix(true, "base64", "copia-e-cola", LocalDateTime.now(), null);
        when(gatewayPagamento.obterQrCodePix("pay_qr")).thenReturn(qrCode);

        // Act
        QrCodePix resultado = pedidoService.obterQrCodeCobrancaPix(1L);

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
        verify(gatewayPagamento, never()).obterQrCodePix(any());
    }

    // ---- Status do pagamento nos eventos do Asaas ----

    private EventoPagamento eventoCobranca(TipoEventoPagamento tipo, Long pedidoId, String motivoRecusa) {
        return new EventoPagamento("evt_000002", tipo, "PAYMENT_" + tipo.name(), LocalDateTime.now(),
                "pay_000001", pedidoId, motivoRecusa);
    }

    @Test
    void processarEventoCobranca_PagamentoConfirmadoDeveMarcarPagamentoComoConfirmado() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.CONFIRMADA, 1L));

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
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECEBIDA, 1L));

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
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECUSADA, 1L, "Saldo insuficiente"));

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
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECUSADA, 1L));

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
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaCriada("pay_000001", new BigDecimal("219.20")));

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        Pagamento pagamento = ultimoPedidoGravado().getPagamento();
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(pagamento.getIdCobranca()).isEqualTo("pay_000001");
    }

    // ---- Data de entrega no fuso da loja ----

    @Test
    void rastreioPedidoWebhook_EntregueDeveMarcarEntregueComADataDoEvento() {
        // Arrange: a conversão de UTC para o fuso da loja é do tradutor (TradutorEventoRastreioTest)
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setStatus(StatusEnum.EM_TRANSITO);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.rastreioPedidoWebhook(pedido, new EventoRastreio(TipoEventoRastreio.ENTREGUE, "order.delivered",
                UUID.randomUUID(), "delivered", "PROTOCOLO", "TRACK123", "https://example.com/tracking", LocalDate.of(2026, 10, 23)));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.ENTREGUE);
        assertThat(pedido.getDataEntrega()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(pedido.getTracking()).isEqualTo("TRACK123");
    }

    // ---- Boleto ----

    private static final java.time.ZoneId FUSO_LOJA = java.time.ZoneId.of("America/Sao_Paulo");

    @Test
    void fazerPedido_BoletoDeveCriarCobrancaComValorVencimentoECancelamentoDoRegistro() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.BOLETO);
        prepararCriacaoDoPedido(pedido);

        // Act
        pedidoService.fazerPedido(pedido);

        // Assert
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        SolicitacaoCobranca request = captor.getValue();
        assertThat(request.tipo()).isEqualTo(TipoPagamentoEnum.BOLETO);
        assertThat(request.valor()).isEqualByComparingTo("221.19");
        assertThat(request.vencimento()).isEqualTo(LocalDate.now(FUSO_LOJA).plusDays(3));
        assertThat(request.diasCancelamentoRegistro()).isEqualTo(1);
        assertThat(request.ipRemoto()).isNull();
        assertThat(request.tokenCartao()).isNull();
        assertThat(request.referenciaPedido()).isEqualTo("10");
    }

    @Test
    void fazerPedido_BoletoDeveGuardarIdUrlEVencimentoComPagamentoPendente() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.BOLETO);
        prepararCriacaoDoPedido(pedido);
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaDeBoleto("pay_boleto", "https://sandbox.asaas.com/b/pdf/pay_boleto"));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        Pagamento pagamento = resultado.getPagamento();
        assertThat(pagamento.getIdCobranca()).isEqualTo("pay_boleto");
        assertThat(pagamento.getUrlBoleto()).isEqualTo("https://sandbox.asaas.com/b/pdf/pay_boleto");
        assertThat(pagamento.getDataVencimento()).isEqualTo(LocalDate.now(FUSO_LOJA).plusDays(3));
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void fazerPedido_BoletoUsaOsDiasDeVencimentoConfigurados() {
        // Arrange
        PedidoService servico = servicoComBoletoVencendoEm(5);
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.BOLETO);
        prepararCriacaoDoPedido(pedido);

        // Act
        servico.fazerPedido(pedido);

        // Assert
        ArgumentCaptor<SolicitacaoCobranca> captor = ArgumentCaptor.forClass(SolicitacaoCobranca.class);
        verify(gatewayPagamento).criarCobranca(captor.capture());
        assertThat(captor.getValue().vencimento()).isEqualTo(LocalDate.now(FUSO_LOJA).plusDays(5));
    }

    private Pedido pedidoDeBoletoComCobranca() {
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.BOLETO));
        pedido.getPagamento().setIdCobranca("pay_boleto");
        pedido.getPagamento().setUrlBoleto("https://sandbox.asaas.com/b/pdf/pay_boleto");
        pedido.getPagamento().setDataVencimento(LocalDate.of(2026, 11, 2));
        return pedido;
    }

    @Test
    void obterBoleto_DeveRetornarLinhaDigitavelCodigoDeBarrasUrlEVencimento() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(pedidoDeBoletoComCobranca());
        when(gatewayPagamento.obterLinhaDigitavel("pay_boleto")).thenReturn(
                new LinhaDigitavel("00190000090123456789012345678901234567890123", "00191234500000221190000001234567890123456789"));

        // Act
        DadosBoleto boleto = pedidoService.obterBoleto(1L);

        // Assert
        assertThat(boleto.linhaDigitavel()).isEqualTo("00190000090123456789012345678901234567890123");
        assertThat(boleto.codigoBarras()).isEqualTo("00191234500000221190000001234567890123456789");
        assertThat(boleto.urlBoleto()).isEqualTo("https://sandbox.asaas.com/b/pdf/pay_boleto");
        assertThat(boleto.dataVencimento()).isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    void obterBoleto_PedidoQueNaoEhBoletoDeveLancarRegraDeNegocio() {
        // Arrange
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.PIX));
        pedido.getPagamento().setIdCobranca("pay_pix");
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obterBoleto(1L)).isInstanceOf(RegraDeNegocioException.class);
        verify(gatewayPagamento, never()).obterLinhaDigitavel(any());
    }

    @Test
    void obterBoleto_SemCobrancaDeveLancarRegraDeNegocio() {
        // Arrange
        Pedido pedido = pedidoDoCliente(1L, criarCliente());
        pedido.setPagamento(Pagamento.pendente(TipoPagamentoEnum.BOLETO));
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.obterBoleto(1L)).isInstanceOf(RegraDeNegocioException.class);
        verify(gatewayPagamento, never()).obterLinhaDigitavel(any());
    }

    // ---- Cobrança vencida (boleto e PIX) ----

    private Pedido pedidoAguardandoPagamento(TipoPagamentoEnum tipo, int estoqueProduto) {
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, estoqueProduto, 2);
        pedido.setPagamento(Pagamento.pendente(tipo));
        pedido.getPagamento().setIdCobranca("pay_000001");
        return pedido;
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = TipoPagamentoEnum.class, names = {"BOLETO", "PIX"})
    void processarEventoCobranca_VencidaDeveCancelarPedidoDevolverEstoqueECancelarPagamento(TipoPagamentoEnum tipo) {
        // Arrange
        Pedido pedido = pedidoAguardandoPagamento(tipo, 8);
        Produto produto = pedido.getProdutosAdicionados().getFirst().getProduto();
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.VENCIDA, 1L));

        // Assert
        Pedido gravado = ultimoPedidoGravado();
        assertThat(gravado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(gravado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10); // 8 + 2
    }

    @Test
    void processarEventoCobranca_VencidaDePedidoJaCanceladoNaoFazNada() {
        // Arrange
        Pedido pedido = pedidoAguardandoPagamento(TipoPagamentoEnum.BOLETO, 10);
        pedido.setStatus(StatusEnum.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.VENCIDA, 1L));

        // Assert
        verify(produtoRepository, never()).update(any());
        verify(pedidoRepository, never()).update(any());
    }

    @Test
    void processarEventoCobranca_VencidaDePedidoJaPagoNaoFazNada() {
        // Arrange
        Pedido pedido = pedidoAguardandoPagamento(TipoPagamentoEnum.BOLETO, 10);
        pedido.setStatus(StatusEnum.PREPARANDO_ENVIO);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act
        pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.VENCIDA, 1L));

        // Assert
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
        verify(produtoRepository, never()).update(any());
        verify(pedidoRepository, never()).update(any());
    }

    // ---- Pagamento depois do cancelamento (compensação atrasada) ----

    @Test
    void processarEventoCobranca_PagamentoDePedidoCanceladoRegistraRecebidoSemReativarESinalizaEstorno() {
        // Arrange
        Pedido pedido = pedidoAguardandoPagamento(TipoPagamentoEnum.BOLETO, 10);
        pedido.setStatus(StatusEnum.CANCELADO);
        Produto produto = pedido.getProdutosAdicionados().getFirst().getProduto();
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(PedidoServiceImpl.class);
        ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> logs = new ch.qos.logback.core.read.ListAppender<>();
        logs.start();
        logger.addAppender(logs);

        try {
            // Act
            pedidoService.processarEventoCobranca(1L, eventoCobranca(TipoEventoPagamento.RECEBIDA, 1L));

            // Assert
            Pedido gravado = ultimoPedidoGravado();
            assertThat(gravado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
            assertThat(gravado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
            assertThat(produto.getEstoque()).isEqualTo(10);
            verify(gatewayFrete, never()).criarEnvio(any());
            assertThat(logs.list)
                    .filteredOn(evento -> evento.getLevel() == ch.qos.logback.classic.Level.ERROR)
                    .extracting(ch.qos.logback.classic.spi.ILoggingEvent::getFormattedMessage)
                    .anyMatch(mensagem -> mensagem.contains("estorno"));
        } finally {
            logger.detachAppender(logs);
        }
    }

    // ---- Cartão: cobrança fora da transação (T1 grava o pedido -> cobra sem transação -> T2 registra) ----

    /** Pedido como relido do banco depois do commit da T1: outra instância, mesmo id. */
    private Pedido pedidoRelidoDoBanco(StatusEnum status, StatusPagamentoEnum statusPagamento) {
        Pedido relido = novoPedido(produtoComEstoque(8), TipoPagamentoEnum.CREDIT_CARD);
        relido.setId(10L);
        relido.setStatus(status);
        relido.getPagamento().setStatus(statusPagamento);
        when(pedidoRepository.findById(10L)).thenReturn(relido);
        return relido;
    }

    @Test
    void fazerPedido_CartaoDeveCobrarForaDeTransacao() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        List<Boolean> cobrouDentroDeTransacao = new ArrayList<>();
        when(gatewayPagamento.criarCobranca(any())).thenAnswer(inv -> {
            cobrouDentroDeTransacao.add(executor.dentroDeTransacao);
            return cobrancaAprovada("pay_000001", new BigDecimal("219.20"));
        });

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        assertThat(cobrouDentroDeTransacao).containsExactly(false);
    }

    @Test
    void fazerPedido_CartaoDeveGravarOPedidoEmTransacaoAntesDeCobrar() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        List<Boolean> gravouDentroDeTransacao = new ArrayList<>();
        when(pedidoRepository.create(any(Pedido.class))).thenAnswer(inv -> {
            gravouDentroDeTransacao.add(executor.dentroDeTransacao);
            Pedido salvo = inv.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        assertThat(gravouDentroDeTransacao).containsExactly(true);
        var ordem = inOrder(pedidoRepository, gatewayPagamento);
        ordem.verify(pedidoRepository).create(any());
        ordem.verify(gatewayPagamento).criarCobranca(any());
    }

    @Test
    void fazerPedido_CartaoAprovadoDeveRegistrarOResultadoNoPedidoRelidoDoBanco() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        CartaoCliente cartao = prepararCartaoTokenizado();
        Pedido relido = pedidoRelidoDoBanco(StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.PENDENTE);
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaAprovada("pay_000001", new BigDecimal("219.20")));
        List<Boolean> registrouDentroDeTransacao = new ArrayList<>();
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(inv -> {
            registrouDentroDeTransacao.add(executor.dentroDeTransacao);
            return inv.getArgument(0);
        });

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        assertThat(ultimoPedidoGravado()).isSameAs(relido);
        assertThat(registrouDentroDeTransacao).containsOnly(true);
        assertThat(relido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.CONFIRMADO);
        assertThat(relido.getPagamento().getIdCobranca()).isEqualTo("pay_000001");
        assertThat(relido.getPagamento().getValorLiquido()).isEqualByComparingTo("219.20");
        assertThat(relido.getPagamento().getCartao()).isSameAs(cartao);
        assertThat(resultado).isSameAs(relido);
    }

    @Test
    void fazerPedido_CartaoComEventoDeConfirmacaoAntesDoRegistroNaoDeveVoltarOStatusDoPedido() {
        // Arrange: o webhook liberou o pedido enquanto a resposta da cobrança voltava
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        Pedido relido = pedidoRelidoDoBanco(StatusEnum.PREPARANDO_ENVIO, StatusPagamentoEnum.RECEBIDO);
        relido.getPagamento().setIdCobranca("pay_000001");
        when(gatewayPagamento.criarCobranca(any())).thenReturn(cobrancaAprovada("pay_000001", new BigDecimal("219.20")));

        // Act
        pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        assertThat(relido.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
        assertThat(relido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.RECEBIDO);
        assertThat(relido.getPagamento().getIdCobranca()).isEqualTo("pay_000001");
    }

    @Test
    void fazerPedido_CartaoSemRespostaDoFornecedorDeveManterPedidoPendenteSemRepetirACobranca() {
        // Arrange
        Produto produto = produtoComEstoque(10);
        Pedido pedido = novoPedido(produto, TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        when(gatewayPagamento.criarCobranca(any())).thenThrow(
                new br.com.fiap.rei_dos_piratas.domain.exceptions.CobrancaSemRespostaException("sem resposta"));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3"));

        // Assert
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
        assertThat(resultado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE);
        assertThat(resultado.getPagamento().getIdCobranca()).isNull();
        assertThat(produto.getEstoque()).isEqualTo(8); // reservado até o evento do Asaas
        verify(gatewayPagamento, times(1)).criarCobranca(any());
    }

    @Test
    void fazerPedido_CartaoComErroDoFornecedorDeveCancelarPedidoDevolverEstoqueEPropagar() {
        // Arrange: o Asaas respondeu com erro que não é recusa -> a cobrança certamente não existe
        Produto produto = produtoComEstoque(10);
        Pedido pedido = novoPedido(produto, TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        when(gatewayPagamento.criarCobranca(any())).thenThrow(
                new br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException("Erro no Asaas"));

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3")))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException.class);
        Pedido gravado = ultimoPedidoGravado();
        assertThat(gravado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(gravado.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10);
    }

    @Test
    void fazerPedido_CartaoComErroNaTokenizacaoDeveCancelarPedidoSemCobrar() {
        // Arrange
        Produto produto = produtoComEstoque(10);
        Pedido pedido = novoPedido(produto, TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        when(cartaoService.obterOuTokenizar(any(), any(), any())).thenThrow(
                new br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException("Erro no Asaas"));

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3")))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.ApiExternaException.class);
        assertThat(ultimoPedidoGravado().getStatus()).isEqualTo(StatusEnum.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(10);
        verify(gatewayPagamento, never()).criarCobranca(any());
    }

    @Test
    void fazerPedido_CartaoRecusadoDeveRegistrarARecusaEmTransacao() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.CREDIT_CARD);
        prepararCriacaoDoPedido(pedido);
        prepararCartaoTokenizado();
        when(gatewayPagamento.criarCobranca(any())).thenThrow(new PagamentoRecusadoException("Transação não autorizada"));
        List<Boolean> registrouDentroDeTransacao = new ArrayList<>();
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(inv -> {
            registrouDentroDeTransacao.add(executor.dentroDeTransacao);
            return inv.getArgument(0);
        });

        // Act
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido, new PagamentoCartao(null, cartaoNovo, 1, "200.1.2.3")))
                .isInstanceOf(PagamentoRecusadoException.class);

        // Assert
        assertThat(registrouDentroDeTransacao).isNotEmpty().containsOnly(true);
    }

    @Test
    void fazerPedido_PixPeloMetodoDoCartaoDeveExecutarEmTransacao() {
        // Arrange: o método do cartão não é transacional; o PIX não pode perder a transação por autochamada
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.PIX);
        prepararCriacaoDoPedido(pedido);
        List<Boolean> cobrouDentroDeTransacao = new ArrayList<>();
        when(gatewayPagamento.criarCobranca(any())).thenAnswer(inv -> {
            cobrouDentroDeTransacao.add(executor.dentroDeTransacao);
            return cobrancaCriada("pay_000001", new BigDecimal("219.20"));
        });

        // Act
        pedidoService.fazerPedido(pedido, null);

        // Assert
        assertThat(cobrouDentroDeTransacao).containsExactly(true);
    }

    // ---- Cobrança sem resposta: o evento completa o id da cobrança ----

    @Test
    void processarEventoCobranca_ConfirmacaoDeCobrancaSemIdDevePreencherComOIdDoEventoELiberarEnvio() {
        // Arrange: o Asaas não respondeu na criação, então o pagamento ficou sem id da cobrança
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        pedido.getPagamento().setIdCobranca(null);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, new EventoPagamento("evt_1", TipoEventoPagamento.CONFIRMADA,
                "PAYMENT_CONFIRMED", LocalDateTime.now(), "pay_do_evento", 1L, null));

        // Assert
        assertThat(pedido.getPagamento().getIdCobranca()).isEqualTo("pay_do_evento");
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamentoEnum.CONFIRMADO);
        assertThat(pedido.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
    }

    @Test
    void processarEventoCobranca_ConfirmacaoNaoDeveSobrescreverOIdDaCobrancaJaGravado() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.processarEventoCobranca(1L, new EventoPagamento("evt_1", TipoEventoPagamento.CONFIRMADA,
                "PAYMENT_CONFIRMED", LocalDateTime.now(), "pay_outro", 1L, null));

        // Assert
        assertThat(pedido.getPagamento().getIdCobranca()).isEqualTo("pay_000001");
    }

    // ---- Escolha do serviço de frete ----

    @Test
    void fazerPedido_ServicoDeFreteQueNaoEhOPrimeiroDaListaDeveSerAceitoComOPrecoDele() {
        // Arrange: bug — só a primeira opção retornada pelo Melhor Envio era considerada
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.PIX);
        pedido.setServicoEntrega(2L);
        prepararCriacaoDoPedido(pedido);
        Transportadora correios = new Transportadora(1L, "Correios", "https://example.com/correios.png");
        when(freteService.calcularFreteProdutos(anyString(), any())).thenReturn(List.of(
                new OpcaoFrete(1L, "PAC", BigDecimal.valueOf(21.19), BigDecimal.valueOf(21.19), BigDecimal.ZERO, "R$", 6, correios),
                new OpcaoFrete(2L, "SEDEX", BigDecimal.valueOf(35.50), BigDecimal.valueOf(35.50), BigDecimal.ZERO, "R$", 2, correios)));

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        assertThat(resultado.getValorFrete()).isEqualByComparingTo("35.50");
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("235.50");
    }

    @Test
    void fazerPedido_ServicoDeFreteForaDaListaDeveLancarNaoEncontradoSemGravar() {
        // Arrange
        Pedido pedido = novoPedido(produtoComEstoque(10), TipoPagamentoEnum.PIX);
        pedido.setServicoEntrega(99L);
        prepararCriacaoDoPedido(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.fazerPedido(pedido)).isInstanceOf(ResourceNotFoundException.class);
        verify(pedidoRepository, never()).create(any());
    }

    // ---- Envio do pedido (caracterizado antes da migração para GatewayFrete) ----

    private Pedido pedidoComFrete(Long id, StatusEnum status, UUID pedidoFrete) {
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(id, 10, 2);
        pedido.setStatus(status);
        pedido.setPedidoFrete(pedidoFrete);
        return pedido;
    }

    @Test
    void pagarPedido_DeveEnviarLojaComoRemetenteClienteComoDestinoProdutosEVolume() {
        // Arrange
        Pedido pedido = pedidoDeCartaoAguardandoPagamento(1L, 10, 2);
        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        prepararCriacaoDeFrete();

        // Act
        pedidoService.pagarPedido(1L);

        // Assert
        ArgumentCaptor<SolicitacaoEnvio> captor = ArgumentCaptor.forClass(SolicitacaoEnvio.class);
        verify(gatewayFrete).criarEnvio(captor.capture());
        SolicitacaoEnvio envio = captor.getValue();
        assertThat(envio.servico()).isEqualTo(1L);
        assertThat(envio.remetente().nome()).isEqualTo("Rei dos Piratas");
        assertThat(envio.remetente().cnpj()).isEqualTo("12345678901234");
        assertThat(envio.remetente().cep()).isEqualTo("12345678");
        assertThat(envio.remetente().uf()).isEqualTo("SP");
        assertThat(envio.destinatario().nome()).isEqualTo("João Silva");
        assertThat(envio.destinatario().cpf()).isEqualTo("52998224725");
        assertThat(envio.destinatario().email()).isEqualTo("joao@gmail.com");
        assertThat(envio.produtos()).singleElement().satisfies(produto -> {
            assertThat(produto.nome()).isEqualTo("Produto Teste");
            assertThat(produto.quantidade()).isEqualTo(2);
            assertThat(produto.valorUnitario()).isEqualByComparingTo("100");
        });
        // < 6 itens: altura = profundidade (0,2 -> 0) x 2 + 3; caixa 16 x 26; peso 0,3 x 2
        assertThat(envio.volume().altura()).isEqualTo(3);
        assertThat(envio.volume().largura()).isEqualTo(16);
        assertThat(envio.volume().comprimento()).isEqualTo(26);
        assertThat(envio.volume().peso()).isEqualByComparingTo("0.6");
        assertThat(envio.valorSeguro()).isEqualByComparingTo("221.19");
        assertThat(envio.lembrete()).isEqualTo("Entrega do pedido 1");
    }

    @Test
    void organizarPedidosParaEnvio_CompraAprovadaDeveMoverParaAguardandoGeracaoDeEtiqueta() {
        // Arrange
        UUID frete = UUID.randomUUID();
        when(pedidoRepository.findByIdsAndStatus(List.of(1L), StatusEnum.PREPARANDO_ENVIO))
                .thenReturn(List.of(pedidoComFrete(1L, StatusEnum.PREPARANDO_ENVIO, frete)));
        when(gatewayFrete.comprarEnvios(List.of(frete))).thenReturn(new ResultadoCompraEnvios(null));

        // Act
        String mensagem = pedidoService.organizarPedidosParaEnvio(List.of(1L));

        // Assert
        assertThat(mensagem).isNull();
        verify(pedidoRepository).updateStatusBatch(List.of(1L), StatusEnum.AGUARDANDO_GERACAO_ETIQUETA);
    }

    @Test
    void organizarPedidosParaEnvio_CompraComMensagemDeErroDeveRetornarAMensagemSemMudarStatus() {
        // Arrange
        UUID frete = UUID.randomUUID();
        when(pedidoRepository.findByIdsAndStatus(List.of(1L), StatusEnum.PREPARANDO_ENVIO))
                .thenReturn(List.of(pedidoComFrete(1L, StatusEnum.PREPARANDO_ENVIO, frete)));
        when(gatewayFrete.comprarEnvios(any())).thenReturn(new ResultadoCompraEnvios("Saldo insuficiente"));

        // Act
        String mensagem = pedidoService.organizarPedidosParaEnvio(List.of(1L));

        // Assert
        assertThat(mensagem).isEqualTo("Saldo insuficiente");
        verify(pedidoRepository, never()).updateStatusBatch(any(), any());
    }

    @Test
    void gerarEtiquetasParaEnvio_DeveInformarOResultadoPorPedidoEAvancarSoOsGerados() {
        // Arrange
        UUID frete1 = UUID.randomUUID();
        UUID frete2 = UUID.randomUUID();
        when(pedidoRepository.findByIdsAndStatus(List.of(1L, 2L), StatusEnum.AGUARDANDO_GERACAO_ETIQUETA)).thenReturn(List.of(
                pedidoComFrete(1L, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA, frete1),
                pedidoComFrete(2L, StatusEnum.AGUARDANDO_GERACAO_ETIQUETA, frete2)));
        when(gatewayFrete.gerarEtiquetas(any())).thenReturn(Map.of(
                frete1, new ResultadoEtiqueta(true, "OK"),
                frete2, new ResultadoEtiqueta(false, "Sem saldo")));

        // Act
        Map<Long, String> resultado = pedidoService.gerarEtiquetasParaEnvio(List.of(1L, 2L));

        // Assert
        assertThat(resultado).containsEntry(1L, "Etiqueta gerada com sucesso")
                .containsEntry(2L, "Erro ao gerar etiqueta: Sem saldo");
        verify(pedidoRepository).updateStatusBatch(List.of(1L), StatusEnum.AGUARDANDO_POSTAGEM);
    }

    @Test
    void imprimirEtiquetasEnvio_DeveRetornarOLinkDoFornecedor() {
        // Arrange
        UUID frete = UUID.randomUUID();
        when(pedidoRepository.findByIdsAndStatus(List.of(1L), StatusEnum.AGUARDANDO_POSTAGEM))
                .thenReturn(List.of(pedidoComFrete(1L, StatusEnum.AGUARDANDO_POSTAGEM, frete)));
        when(gatewayFrete.obterLinkImpressao(List.of(frete))).thenReturn("https://example.com/etiquetas.pdf");

        // Act & Assert
        assertThat(pedidoService.imprimirEtiquetasEnvio(List.of(1L))).isEqualTo("https://example.com/etiquetas.pdf");
    }
}
