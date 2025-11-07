package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CarrinhoControllerImplTest {

    private CarrinhoService carrinhoService;
    private ProdutoService produtoService;
    private CarrinhoController carrinhoController;

    @BeforeEach
    void setUp() {
        this.carrinhoService = mock(CarrinhoService.class);
        this.produtoService = mock(ProdutoService.class);
        this.carrinhoController = new CarrinhoControllerImpl(carrinhoService, produtoService);
    }

    @Test
    void adicionarProduto() {
        // Arrange
        Long clienteId = 1L;
        Long produtoId = 1L;
        int quantidade = 2;

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
                2000.00F);

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece",
                "Action figure do Luffy em alta qualidade",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoCarrinho item = new ItemProdutoCarrinho(produto, quantidade);

        Carrinho carrinho = new Carrinho(1L, List.of(item));

        when(produtoService.findById(produtoId)).thenReturn(produto);
        when(carrinhoService.adicionarProduto(any(ItemProdutoPedido.class))).thenReturn(carrinho);

        // Act
        carrinhoController.adicionarProduto(new br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto(produtoId, quantidade));

        // Assert
        verify(produtoService, times(1)).findById(produtoId);
        verify(carrinhoService, times(1)).adicionarProduto(any(ItemProdutoPedido.class));
    }

    @Test
    void removerProduto() {
        // Arrange
        Long clienteId = 1L;
        Long produtoId = 1L;
        int quantidade = 1;

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
                2000.00F);

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece",
                "Action figure do Luffy em alta qualidade",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        when(produtoService.findById(produtoId)).thenReturn(produto);
        when(carrinhoService.removerProduto(any(ItemProdutoPedido.class))).thenReturn(carrinho);

        // Act
        carrinhoController.removerProduto(new br.com.fiap.rei_dos_piratas.interfaces.dto.ItemProdutoInDto(produtoId, quantidade));

        // Assert
        verify(produtoService, times(1)).findById(produtoId);
        verify(carrinhoService, times(1)).removerProduto(any(ItemProdutoPedido.class));
    }

    @Test
    void limparCarrinho() {
        // Arrange
        Long clienteId = 1L;

        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        when(carrinhoService.limparCarrinho()).thenReturn(carrinho);

        // Act
        carrinhoController.limparCarrinho();

        // Assert
        verify(carrinhoService, times(1)).limparCarrinho();
    }

    @Test
    void visualizarCarrinho() {
        // Arrange
        Long clienteId = 1L;

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
                2000.00F);

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece",
                "Action figure do Luffy em alta qualidade",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoCarrinho item = new ItemProdutoCarrinho(produto, 2);

        Carrinho carrinho = new Carrinho(1L, List.of(item));

        when(carrinhoService.visualizarCarrinho()).thenReturn(carrinho);

        // Act
        carrinhoController.visualizarCarrinho();

        // Assert
        verify(carrinhoService, times(1)).visualizarCarrinho();
    }

    @Test
    void finalizarCompra() {
        // Arrange
        Long clienteId = 1L;

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
                2000.00F);

        Produto produto = new Produto(
                1L,
                "Action Figure One Piece",
                "Action figure do Luffy em alta qualidade",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        ItemProdutoPedido item = new ItemProdutoPedido(produto, 2);

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
                clienteId,
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

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setProdutosAdicionados(List.of(item));
        pedido.setStatus(StatusEnum.AGUARDANDO_PAGAMENTO);
        pedido.setDataPedido(LocalDate.now());
        pedido.setValorTotal(300.00F);

        when(carrinhoService.finalizarCompra()).thenReturn(pedido);

        // Act
        carrinhoController.finalizarCompra();

        // Assert
        verify(carrinhoService, times(1)).finalizarCompra();
    }
}
