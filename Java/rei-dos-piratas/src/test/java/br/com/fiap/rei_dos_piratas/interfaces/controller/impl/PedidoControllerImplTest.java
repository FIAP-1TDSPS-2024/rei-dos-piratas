package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoControllerImplTest {

    private PedidoService pedidoService;
    private ClienteService clienteService;
    private ProdutoService produtoService;
    private PedidoController pedidoController;

    @BeforeEach
    void setUp() {
        this.pedidoService = mock(PedidoService.class);
        this.clienteService = mock(ClienteService.class);
        this.produtoService = mock(ProdutoService.class);
        this.pedidoController = new PedidoControllerImpl(pedidoService, clienteService, produtoService);
    }

    @Test
    void findAllByCliente() {
        // Arrange
        Long clienteId = 1L;

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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedido = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                BigDecimal.valueOf(300),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                cliente,
                List.of(item));

        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(pedido);

        Page<Pedido> pedidoPage = new Page<>(1, 0, pedidos);

        when(pedidoService.findAll(0, 10)).thenReturn(pedidoPage);

        // Act
        final Page<Pedido> foundPedidoPage = pedidoService.findAll(0, 10);

        // Assert
        verify(pedidoService, times(1)).findAll(0, 10);
        assertThat(foundPedidoPage).isSameAs(pedidoPage);
    }

    @Test
    void findById() {
        // Arrange
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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedido = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                BigDecimal.valueOf(300),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                cliente,
                List.of(item));

        when(pedidoService.findById(1L)).thenReturn(pedido);

        // Act
        final Pedido foundPedido = pedidoService.findById(1L);

        // Assert
        verify(pedidoService, times(1)).findById(1L);
        assertThat(foundPedido).isSameAs(pedido);
    }

    @Test
    void fazerPedido() {
        // Arrange
        Long clienteId = 1L;

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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(produto, 2);

        Pedido pedidoCriado = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                BigDecimal.valueOf(300),
                StatusEnum.AGUARDANDO_PAGAMENTO,
                cliente,
                List.of(item));

        when(clienteService.findById(clienteId)).thenReturn(cliente);
        when(produtoService.findById(1L)).thenReturn(produto);
        when(pedidoService.fazerPedido(any(Pedido.class))).thenReturn(pedidoCriado);

        // Act
        final Pedido newPedido = pedidoService.fazerPedido(new Pedido(cliente, List.of(item)));

        // Assert
        verify(pedidoService, times(1)).fazerPedido(any(Pedido.class));
        assertThat(newPedido).isSameAs(pedidoCriado);
    }

    @Test
    void pagarPedido() {
        // Arrange
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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedidoPago = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                BigDecimal.valueOf(300),
                StatusEnum.PREPARANDO_ENVIO,
                cliente,
                List.of(item));

        when(pedidoService.pagarPedido(1L)).thenReturn(pedidoPago);

        // Act
        final Pedido pedidoAtualizado = pedidoService.pagarPedido(1L);

        // Assert
        verify(pedidoService, times(1)).pagarPedido(1L);
        assertThat(pedidoAtualizado).isSameAs(pedidoPago);
        assertThat(pedidoAtualizado.getStatus()).isEqualTo(StatusEnum.PREPARANDO_ENVIO);
    }

    @Test
    void enviarPedido() {
        // Arrange
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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedidoEnviado = new Pedido(
                1L,
                LocalDate.now(),
                null,
                null,
                BigDecimal.valueOf(300),
                StatusEnum.EM_TRANSITO,
                cliente,
                List.of(item));

        when(pedidoService.enviarPedido(1L)).thenReturn(pedidoEnviado);

        // Act
        final Pedido pedidoAtualizado = pedidoService.enviarPedido(1L);

        // Assert
        verify(pedidoService, times(1)).enviarPedido(1L);
        assertThat(pedidoAtualizado).isSameAs(pedidoEnviado);
        assertThat(pedidoAtualizado.getStatus()).isEqualTo(StatusEnum.EM_TRANSITO);
    }

    @Test
    void entregarPedido() {
        // Arrange
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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedidoEntregue = new Pedido(
                1L,
                LocalDate.now(),
                LocalDate.now(),
                null,
                BigDecimal.valueOf(300),
                StatusEnum.ENTREGUE,
                cliente,
                List.of(item));

        when(pedidoService.entregarPedido(1L)).thenReturn(pedidoEntregue);

        // Act
        final Pedido pedidoAtualizado = pedidoService.entregarPedido(1L);

        // Assert
        verify(pedidoService, times(1)).entregarPedido(1L);
        assertThat(pedidoAtualizado).isSameAs(pedidoEntregue);
        assertThat(pedidoAtualizado.getStatus()).isEqualTo(StatusEnum.ENTREGUE);
    }

    @Test
    void cancelarPedido() {
        // Arrange
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

        Cliente cliente = new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                endereco,
                "12345678978",
                new Carrinho());

        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "João Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 2);

        Pedido pedidoCancelado = new Pedido(
                1L,
                LocalDate.now(),
                null,
                LocalDate.now(),
                BigDecimal.valueOf(300),
                StatusEnum.CANCELADO,
                cliente,
                List.of(item));

        when(pedidoService.cancelarPedido(1L)).thenReturn(pedidoCancelado);

        // Act
        final Pedido pedidoAtualizado = pedidoService.cancelarPedido(1L);

        // Assert
        verify(pedidoService, times(1)).cancelarPedido(1L);
        assertThat(pedidoAtualizado).isSameAs(pedidoCancelado);
        assertThat(pedidoAtualizado.getStatus()).isEqualTo(StatusEnum.CANCELADO);
    }
}
