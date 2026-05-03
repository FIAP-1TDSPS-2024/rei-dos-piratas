package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.repository.CarrinhoRepository;
import br.com.fiap.rei_dos_piratas.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CarrinhoServiceImplTest {

    private CarrinhoService carrinhoService;
    private CarrinhoRepository carrinhoRepository;
    private PedidoService pedidoService;
    private ProdutoService produtoService;
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {

        CustomUserDetails mockCliente = new CustomUserDetails(
                1L,
                "joaosilva",
                "SenhaSegura123",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockCliente);

        SecurityContextHolder.setContext(securityContext);

        this.carrinhoRepository = mock(CarrinhoRepository.class);
        this.pedidoService = mock(PedidoService.class);
        this.produtoService = mock(ProdutoService.class);
        this.clienteService = mock(ClienteService.class);
        this.carrinhoService = new CarrinhoServiceImpl(carrinhoRepository, pedidoService, produtoService, clienteService);
    }

    @Test
    void adicionarProduto_DeveAdicionarProdutoAoCarrinho() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());
        Perfil perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);

        Cliente cliente = new Cliente(
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
                "11991231234",
                carrinho);

        ItemProdutoPedido itemProdutoPedido = new ItemProdutoPedido(produto, 2);

        Carrinho carrinhoAtualizado = new Carrinho(1L, new ArrayList<>());
        carrinhoAtualizado.getProdutosAdicionados().add(new ItemProdutoCarrinho(produto, 2));

        when(produtoService.findById(1L)).thenReturn(produto);
        when(clienteService.findById(1L)).thenReturn(cliente);
        when(carrinhoRepository.update(any(Carrinho.class))).thenReturn(carrinhoAtualizado);

        // Act
        Carrinho resultado = carrinhoService.adicionarProduto(itemProdutoPedido);

        // Assert
        verify(produtoService, times(1)).findById(1L);
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, times(1)).update(any(Carrinho.class));
        assertThat(resultado.getProdutosAdicionados()).hasSize(1);
        assertThat(resultado.getProdutosAdicionados().get(0).getProduto()).isEqualTo(produto);
        assertThat(resultado.getProdutosAdicionados().get(0).getQuantidade()).isEqualTo(2);
    }

    @Test
    void adicionarProduto_DeveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de cliente", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        ItemProdutoPedido itemProdutoPedido = new ItemProdutoPedido(produto, 5); // Quantidade maior que estoque

        when(produtoService.findById(1L)).thenReturn(produto);
        when(clienteService.findById(1L)).thenReturn(cliente);

        // Act & Assert
        assertThatThrownBy(() -> carrinhoService.adicionarProduto(itemProdutoPedido))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(produtoService, times(1)).findById(1L);
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, never()).update(any(Carrinho.class));
    }

    @Test
    void removerProduto_DeveRemoverProdutoDoCarrinho() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        List<ItemProdutoCarrinho> produtosNoCarrinho = new ArrayList<>();
        produtosNoCarrinho.add(new ItemProdutoCarrinho(1L, produto, 5));

        Carrinho carrinho = new Carrinho(1L, produtosNoCarrinho);

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de cliente", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        ItemProdutoPedido itemProdutoPedido = new ItemProdutoPedido(produto, 2);

        Carrinho carrinhoAtualizado = new Carrinho(1L, produtosNoCarrinho);

        when(produtoService.findById(1L)).thenReturn(produto);
        when(clienteService.findById(1L)).thenReturn(cliente);
        when(carrinhoRepository.update(any(Carrinho.class))).thenReturn(carrinhoAtualizado);

        // Act
        Carrinho resultado = carrinhoService.removerProduto(itemProdutoPedido);

        // Assert
        verify(produtoService, times(1)).findById(1L);
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, times(1)).update(any(Carrinho.class));
        assertThat(resultado.getProdutosAdicionados()).hasSize(1);
        assertThat(resultado.getProdutosAdicionados().get(0).getQuantidade()).isEqualTo(3);
    }

    @Test
    void removerProduto_DeveRemoverProdutoCompletoQuandoQuantidadeIgualOuMaior() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        List<ItemProdutoCarrinho> produtosNoCarrinho = new ArrayList<>();
        produtosNoCarrinho.add(new ItemProdutoCarrinho(1L, produto, 3));

        Carrinho carrinho = new Carrinho(1L, produtosNoCarrinho);

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        ItemProdutoPedido itemProdutoPedido = new ItemProdutoPedido(produto, 5);

        List<ItemProdutoCarrinho> carrinhoVazio = new ArrayList<>();
        Carrinho carrinhoAtualizado = new Carrinho(1L, carrinhoVazio);

        when(produtoService.findById(1L)).thenReturn(produto);
        when(clienteService.findById(1L)).thenReturn(cliente);
        when(carrinhoRepository.update(any(Carrinho.class))).thenReturn(carrinhoAtualizado);

        // Act
        Carrinho resultado = carrinhoService.removerProduto(itemProdutoPedido);

        // Assert
        verify(produtoService, times(1)).findById(1L);
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, times(1)).update(any(Carrinho.class));
        assertThat(resultado.getProdutosAdicionados()).isEmpty();
    }

    @Test
    void removerProduto_DeveLancarExcecaoQuandoProdutoNaoEstaNoCarrinho() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        ItemProdutoPedido itemProdutoPedido = new ItemProdutoPedido(produto, 1);

        when(produtoService.findById(1L)).thenReturn(produto);
        when(clienteService.findById(1L)).thenReturn(cliente);

        // Act & Assert
        assertThatThrownBy(() -> carrinhoService.removerProduto(itemProdutoPedido))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Esse produto não foi incluído no carrinho");

        verify(produtoService, times(1)).findById(1L);
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, never()).update(any(Carrinho.class));
    }

    @Test
    void limparCarrinho_DeveLimparTodosOsProdutosDoCarrinho() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto1 = new Produto(
                1L,
                "Produto Teste 1",
                "Descrição do produto teste 1",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto1.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        Produto produto2 = new Produto(
                2L,
                "Produto Teste 2",
                "Descrição do produto teste 2",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto2.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO,
                funcionario);

        List<ItemProdutoCarrinho> produtosNoCarrinho = new ArrayList<>();
        produtosNoCarrinho.add(new ItemProdutoCarrinho(1L, produto1, 2));
        produtosNoCarrinho.add(new ItemProdutoCarrinho(2L, produto2, 3));

        Carrinho carrinho = new Carrinho(1L, produtosNoCarrinho);

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        Carrinho carrinhoLimpo = new Carrinho(1L, new ArrayList<>());

        when(clienteService.findById(1L)).thenReturn(cliente);
        when(carrinhoRepository.update(any(Carrinho.class))).thenReturn(carrinhoLimpo);

        // Act
        Carrinho resultado = carrinhoService.limparCarrinho();

        // Assert
        verify(clienteService, times(1)).findById(1L);
        verify(carrinhoRepository, times(1)).update(any(Carrinho.class));
        assertThat(resultado.getProdutosAdicionados()).isEmpty();
    }

    @Test
    void visualizarCarrinho_DeveRetornarCarrinhoDoCliente() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        List<ItemProdutoCarrinho> produtosNoCarrinho = new ArrayList<>();
        produtosNoCarrinho.add(new ItemProdutoCarrinho(1L, produto, 2));

        Carrinho carrinho = new Carrinho(1L, produtosNoCarrinho);

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        when(clienteService.findById(1L)).thenReturn(cliente);

        // Act
        Carrinho resultado = carrinhoService.visualizarCarrinho();

        // Assert
        verify(clienteService, times(1)).findById(1L);
        assertThat(resultado).isSameAs(carrinho);
        assertThat(resultado.getProdutosAdicionados()).hasSize(1);
        assertThat(resultado.getProdutosAdicionados().get(0).getProduto()).isEqualTo(produto);
    }

    @Test
    void finalizarCompra_DeveCriarPedidoELimparCarrinho() {
        // Arrange
        Funcionario funcionario = new Funcionario(
                "admin",
                1L,
                "Admin User",
                "admin@example.com",
                "senha123",
                true,
                LocalDate.now(),
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(5000));

        Produto produto = new Produto(
                1L,
                "Produto Teste",
                "Descrição do produto teste",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        List<ItemProdutoCarrinho> produtosNoCarrinho = new ArrayList<>();
        produtosNoCarrinho.add(new ItemProdutoCarrinho(1L, produto, 2));

        Carrinho carrinho = new Carrinho(1L, produtosNoCarrinho);

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinho);

        List<ItemProdutoPedido> produtosPedido = new ArrayList<>();
        produtosPedido.add(new ItemProdutoPedido(1L, produto, 2));

        Estado estado = new Estado(
                1L,
                "São Paulo",
                "SP");

        Cidade cidade = new Cidade(
                1L,
                "São Paulo",
                estado);

        Endereco endereco = new Endereco(
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

        Pedido pedidoEsperado = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                null,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(0),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                cliente,
                produtosPedido,
                endereco,
                3L,
                null,
                null);

        Long freteServiceId = 3L;

        Carrinho carrinhoLimpo = new Carrinho(1L, new ArrayList<>());

        when(clienteService.findById(1L)).thenReturn(cliente);
        when(pedidoService.fazerPedido(any(Pedido.class))).thenReturn(pedidoEsperado);
        when(carrinhoRepository.update(any(Carrinho.class))).thenReturn(carrinhoLimpo);

        // Act
        Pedido resultado = carrinhoService.finalizarCompra(endereco, freteServiceId);

        // Assert
        verify(clienteService, times(2)).findById(1L); // Uma para finalizar e outra para limpar
        verify(pedidoService, times(1)).fazerPedido(any(Pedido.class));
        verify(carrinhoRepository, times(1)).update(any(Carrinho.class));
        assertThat(resultado).isEqualTo(pedidoEsperado);
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        assertThat(resultado.getStatus()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void finalizarCompra_DeveLancarExcecaoQuandoCarrinhoVazio() {
        // Arrange
        Carrinho carrinhoVazio = new Carrinho(1L, new ArrayList<>());

        Cliente cliente = new Cliente(
                1L,
                "joaosilva",
                "João Silva",
                "joao@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                new Perfil(1L, "CLIENT", "Perfil de usuário", null),
                LocalDate.of(1990, 5, 15),
                SexoEnum.M,
                "52998224725",
                "11991231234",
                carrinhoVazio);

        Estado estado = new Estado(
                1L,
                "São Paulo",
                "SP");

        Cidade cidade = new Cidade(
                1L,
                "São Paulo",
                estado);

        Endereco endereco = new Endereco(
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

        when(clienteService.findById(1L)).thenReturn(cliente);

        // Act & Assert
        assertThatThrownBy(() -> carrinhoService.finalizarCompra(endereco, 3L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("O carrinho está vazio");

        verify(clienteService, times(1)).findById(1L);
        verify(pedidoService, never()).fazerPedido(any(Pedido.class));
        verify(carrinhoRepository, never()).update(any(Carrinho.class));
    }
}
