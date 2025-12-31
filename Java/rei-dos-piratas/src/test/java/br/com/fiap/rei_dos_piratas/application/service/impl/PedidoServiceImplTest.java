package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.WrongStatusException;
import br.com.fiap.rei_dos_piratas.domain.repository.PedidoRepository;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PedidoServiceImplTest {

    private PedidoService pedidoService;
    private PedidoRepository pedidoRepository;
    private ProdutoRepository produtoRepository;
    private br.com.fiap.rei_dos_piratas.application.service.ClienteService clienteService;

    @BeforeEach
    void setUp() {

        // Limpa qualquer contexto anterior
        SecurityContextHolder.clearContext();

        // Cria usuário cliente
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_CLIENTE");
        CustomUserDetails userDetails = new CustomUserDetails(1L, "joao", "pwd", List.of(auth));
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        this.pedidoRepository = mock(PedidoRepository.class);
        this.produtoRepository = mock(ProdutoRepository.class);
        this.clienteService = mock(br.com.fiap.rei_dos_piratas.application.service.ClienteService.class);
        this.pedidoService = new PedidoServiceImpl(pedidoRepository, produtoRepository, clienteService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Cliente criarCliente() {
        Endereco endereco = new Endereco(
                1L,
                12345,
                "12345678",
                "Avenida Paulista",
                "Bela Vista",
                10L,
                "São Paulo",
                20L,
                "São Paulo",
                "SP",
                "Brasil",
                "BR");

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        return new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                endereco,
                "12345678900",
                carrinho);
    }

    private Funcionario criarFuncionario() {
        return new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.ADMIN,
                null,
                BigDecimal.valueOf(1000));
    }

    private Produto criarProduto(Long id, String nome, int estoque, BigDecimal preco) {
        return new Produto(
                id,
                nome,
                "Descrição do produto " + nome,
                "http://imagem.com/" + nome + ".jpg",
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
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido1 = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);
        Pedido pedido2 = new Pedido(2L, LocalDate.now(), null, null, BigDecimal.valueOf(300),
                StatusEnum.EM_TRANSITO, cliente, itens);

        List<Pedido> pedidos = List.of(pedido1, pedido2);
        Page<Pedido> page = new Page<>(1, 0, pedidos);

        // Mock do repository
        when(pedidoRepository.listAllByClient(0, 10, 1L)).thenReturn(page);

        // Cria CustomUserDetails com ROLE_CLIENTE corretamente
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                "joao",
                "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // Define autenticação no SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // Act
        Page<Pedido> resultado = pedidoService.findAll(0, 10);

        // Assert
        verify(pedidoRepository, times(1)).listAllByClient(0, 10, 1L);
        assertThat(resultado.pageItems()).hasSize(2);
        assertThat(resultado.pageNumber()).isEqualTo(0);
        assertThat(resultado.numberOfPages()).isEqualTo(1);

        // Limpa contexto para evitar interferência em outros testes
        SecurityContextHolder.clearContext();
    }


    @Test
    void findById_DeveRetornarPedidoQuandoExistir() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

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
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        Pedido pedidoCriado = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        when(pedidoRepository.create(any(Pedido.class))).thenReturn(pedidoCriado);

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        verify(produtoRepository, times(1)).update(produto);
        verify(pedidoRepository, times(1)).create(pedido);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(produto.getEstoque()).isEqualTo(8); // 10 - 2
    }

    @Test
    void fazerPedido_DeveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 1, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 5)); // Quantidade maior que estoque

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, BigDecimal.valueOf(500),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

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
        Produto produto1 = criarProduto(1L, "Produto 1", 10, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto 2", 5, BigDecimal.valueOf(150));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto1, 2));
        itens.add(new ItemProdutoPedido(produto2, 1));

        Pedido pedido = new Pedido(null, LocalDate.now(), null, null, BigDecimal.valueOf(350),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        Pedido pedidoCriado = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(350),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        when(pedidoRepository.create(any(Pedido.class))).thenReturn(pedidoCriado);

        // Act
        Pedido resultado = pedidoService.fazerPedido(pedido);

        // Assert
        verify(produtoRepository, times(2)).update(any(Produto.class));
        verify(pedidoRepository, times(1)).create(pedido);
        assertThat(produto1.getEstoque()).isEqualTo(8);
        assertThat(produto2.getEstoque()).isEqualTo(4);
    }

    @Test
    void pagarPedido_DeveAtualizarStatusParaPreparandoEnvio() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.pagarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).update(pedido);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
    }

    @Test
    void pagarPedido_DeveLancarExcecaoQuandoStatusIncorreto() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.EM_TRANSITO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.pagarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido deve estar no estado AGUARDANDO_PAGAMENTO");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }

    @Test
    void enviarPedido_DeveAtualizarStatusParaEmTransito() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.PREPARANDO_ENVIO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.enviarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).update(pedido);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
    }

    @Test
    void enviarPedido_DeveLancarExcecaoQuandoStatusIncorreto() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.enviarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido deve estar no estado PREPARANDO_ENVIO");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }

    @Test
    void entregarPedido_DeveAtualizarStatusParaEntregue() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.EM_TRANSITO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);
        when(pedidoRepository.update(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido resultado = pedidoService.entregarPedido(1L);

        // Assert
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).update(pedido);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.ENTREGUE);
    }

    @Test
    void entregarPedido_DeveLancarExcecaoQuandoStatusIncorreto() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.PREPARANDO_ENVIO, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.entregarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido deve estar no estado EM_TRANSITO");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }

    @Test
    void cancelarPedido_DeveCancelarPedidoEDevolverEstoque() {
        // Arrange
        Cliente cliente = criarCliente();
        Produto produto = criarProduto(1L, "Produto Teste", 8, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.AGUARDANDO_PAGAMENTO, cliente, itens);

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
        Produto produto1 = criarProduto(1L, "Produto 1", 8, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto 2", 4, BigDecimal.valueOf(150));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(produto1, 2));
        itens.add(new ItemProdutoPedido(produto2, 1));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(350),
                StatusEnum.PREPARANDO_ENVIO, cliente, itens);

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
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.CANCELADO, cliente, itens);

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
        Produto produto = criarProduto(1L, "Produto Teste", 10, BigDecimal.valueOf(100));

        List<ItemProdutoPedido> itens = new ArrayList<>();
        itens.add(new ItemProdutoPedido(1L, produto, 2));

        Pedido pedido = new Pedido(1L, LocalDate.now(), null, null, BigDecimal.valueOf(200),
                StatusEnum.ENTREGUE, cliente, itens);

        when(pedidoRepository.findById(1L)).thenReturn(pedido);

        // Act & Assert
        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(WrongStatusException.class)
                .hasMessageContaining("O pedido já foi entregue");

        verify(pedidoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).update(any(Produto.class));
        verify(pedidoRepository, never()).update(any(Pedido.class));
    }
}
