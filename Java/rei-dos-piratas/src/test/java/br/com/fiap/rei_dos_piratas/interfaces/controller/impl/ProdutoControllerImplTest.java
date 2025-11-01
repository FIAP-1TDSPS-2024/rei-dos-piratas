package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoControllerImplTest {

    private ProdutoService produtoService;
    private FuncionarioService funcionarioService;
    private ProdutoController produtoController;

    @BeforeEach
    void setUp() {
        this.produtoService = mock(ProdutoService.class);
        this.funcionarioService = mock(FuncionarioService.class);
        this.produtoController = new ProdutoControllerImpl(produtoService, funcionarioService);
    }

    @Test
    void findById() {
        // Arrange
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
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        when(produtoService.findById(1L)).thenReturn(produto);

        // Act
        final Produto foundProduto = produtoService.findById(1L);

        // Assert
        verify(produtoService, times(1)).findById(1L);
        assertThat(foundProduto).isSameAs(produto);
    }

    @Test
    void findAll() {
        // Arrange
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

        Produto produto1 = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        Produto produto2 = new Produto(
                2L,
                "Action Figure One Piece Zoro Sanzen Sekai",
                "Action figure do Zoro com as três espadas em posição de ataque",
                "http://imagem.com/zoro.jpg",
                180.00F,
                5,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        List<Produto> produtos = new ArrayList<>();
        produtos.add(produto1);
        produtos.add(produto2);

        Page<Produto> produtoPage = new Page<>(1, 0, produtos);

        when(produtoService.findAll(0, 10)).thenReturn(produtoPage);

        // Act
        final Page<Produto> foundProdutoPage = produtoService.findAll(0, 10);

        // Assert
        verify(produtoService, times(1)).findAll(0, 10);
        assertThat(foundProdutoPage).isSameAs(produtoPage);
    }

    @Test
    void create() {
        // Arrange
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

        Produto produtoParaCriar = new Produto(
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        Produto produtoCriado = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        when(funcionarioService.findById(1L)).thenReturn(funcionario);
        when(produtoService.create(any(Produto.class))).thenReturn(produtoCriado);

        // Act
        final Produto newProduto = produtoService.create(produtoParaCriar);

        // Assert
        verify(produtoService, times(1)).create(any(Produto.class));
        assertThat(newProduto).isSameAs(produtoCriado);
    }

    @Test
    void update() {
        // Arrange
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

        Produto produtoAntigo = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incríveis",
                "http://imagem.com/luffy.jpg",
                150.00F,
                10,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        Produto produtoNovo = new Produto(
                1L,
                "Action Figure One Piece Luffy Gear 5 Edição Especial",
                "Action figure do Luffy em alta qualidade com detalhes incríveis edição especial",
                "http://imagem.com/luffy-special.jpg",
                200.00F,
                15,
                30F,
                20F,
                15F,
                CondicaoEnum.NOVO,
                funcionario);

        when(produtoService.findById(1L)).thenReturn(produtoAntigo);
        when(produtoService.create(produtoNovo)).thenReturn(produtoNovo);

        // Act
        final Produto updatedProduto = produtoService.create(produtoNovo);

        // Assert
        verify(produtoService, times(1)).create(any(Produto.class));
        assertThat(updatedProduto).isSameAs(produtoNovo).isNotSameAs(produtoAntigo);
    }

    @Test
    void delete() {
        // Arrange
        doNothing().when(produtoService).delete(1L);

        // Act
        produtoService.delete(1L);

        // Assert
        verify(produtoService, times(1)).delete(1L);
    }
}

