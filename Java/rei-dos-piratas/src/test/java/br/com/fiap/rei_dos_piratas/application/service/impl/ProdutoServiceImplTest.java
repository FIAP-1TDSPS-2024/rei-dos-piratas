package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProdutoServiceImplTest {

    private ProdutoService produtoService;
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        this.produtoRepository = mock(ProdutoRepository.class);
        this.produtoService = new ProdutoServiceImpl(produtoRepository);
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
                new Perfil(1L, "ADMIN", "Perfil de administrador", null),
                null,
                BigDecimal.valueOf(1000.00));
    }

    private Produto criarProduto(Long id, String nome, String autor, String categoria, int estoque, BigDecimal preco) {
        return new Produto(
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
    void findById_DeveRetornarProdutoQuandoExistir() {
        // Arrange
        Produto produto = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        when(produtoRepository.findById(1L)).thenReturn(produto);

        // Act
        Produto resultado = produtoService.findById(1L);

        // Assert
        verify(produtoRepository, times(1)).findById(1L);
        assertThat(resultado).isEqualTo(produto);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Produto Teste");
        assertThat(resultado.getEstoque()).isEqualTo(10);
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void findById_DeveLancarExcecaoQuandoNaoExistir() {
        // Arrange
        when(produtoRepository.findById(999L)).thenThrow(new NoSuchElementException());

        // Act & Assert
        assertThatThrownBy(() -> produtoService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Não foi possível encontrar um produto com o id 999");

        verify(produtoRepository, times(1)).findById(999L);
    }

    @Test
    void findAll_DeveRetornarPaginaDeProdutos() {
        // Arrange
        Produto produto1 = criarProduto(1L, "Produto 1", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto 2", "Jonas Oliveira", "ACAO", 5, BigDecimal.valueOf(100));
        Produto produto3 = criarProduto(3L, "Produto 3", "Jonas Oliveira", "ACAO", 20, BigDecimal.valueOf(100));

        List<Produto> produtos = List.of(produto1, produto2, produto3);
        Page<Produto> page = new Page<>(1, 0, produtos);

        when(produtoRepository.listAll(0, 10)).thenReturn(page);

        // Act
        Page<Produto> resultado = produtoService.findAll(0, 10);

        // Assert
        verify(produtoRepository, times(1)).listAll(0, 10);
        assertThat(resultado.pageItems()).hasSize(3);
        assertThat(resultado.pageNumber()).isEqualTo(0);
        assertThat(resultado.numberOfPages()).isEqualTo(1);
    }

    @Test
    void findAll_DeveRetornarPaginaVaziaQuandoNaoHouverProdutos() {
        // Arrange
        Page<Produto> pageVazia = new Page<>(0, 0, List.of());
        when(produtoRepository.listAll(0, 10)).thenReturn(pageVazia);

        // Act
        Page<Produto> resultado = produtoService.findAll(0, 10);

        // Assert
        verify(produtoRepository, times(1)).listAll(0, 10);
        assertThat(resultado.pageItems()).isEmpty();
        assertThat(resultado.numberOfPages()).isEqualTo(0);
    }

    @Test
    void findAll_DeveRetornarPaginaComTamanhoDiferente() {
        // Arrange
        Produto produto1 = criarProduto(1L, "Produto 1", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        Produto produto2 = criarProduto(2L, "Produto 2", "Jonas Oliveira", "ACAO", 5, BigDecimal.valueOf(150));

        List<Produto> produtos = List.of(produto1, produto2);
        Page<Produto> page = new Page<>(5, 1, produtos);

        when(produtoRepository.listAll(1, 2)).thenReturn(page);

        // Act
        Page<Produto> resultado = produtoService.findAll(1, 2);

        // Assert
        verify(produtoRepository, times(1)).listAll(1, 2);
        assertThat(resultado.pageItems()).hasSize(2);
        assertThat(resultado.pageNumber()).isEqualTo(1);
        assertThat(resultado.numberOfPages()).isEqualTo(5);
    }

    @Test
    void create_DeveCriarNovoProduto() {
        // Arrange
        Produto produtoNovo = criarProduto(null, "Produto Novo", "Jonas Oliveira", "ACAO", 15, BigDecimal.valueOf(250));
        Produto produtoCriado = criarProduto(1L, "Produto Novo", "Jonas Oliveira", "ACAO", 15, BigDecimal.valueOf(250));

        when(produtoRepository.create(produtoNovo)).thenReturn(produtoCriado);

        // Act
        Produto resultado = produtoService.create(produtoNovo);

        // Assert
        verify(produtoRepository, times(1)).create(produtoNovo);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Produto Novo");
        assertThat(resultado.getEstoque()).isEqualTo(15);
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(250));
    }

    @Test
    void create_DeveCriarProdutoComCondicaoUsado() {
        // Arrange
        Funcionario funcionario = criarFuncionario();
        Produto produtoUsado = new Produto(
                null,
                "Produto Usado",
                "Descrição produto usado",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/usado.jpg",
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(80),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO,
                funcionario);

        Produto produtoCriado = new Produto(
                1L,
                "Produto Usado",
                "Descrição produto usado",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/usado.jpg",
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(80),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO,
                funcionario);

        when(produtoRepository.create(produtoUsado)).thenReturn(produtoCriado);

        // Act
        Produto resultado = produtoService.create(produtoUsado);

        // Assert
        verify(produtoRepository, times(1)).create(produtoUsado);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCondicao()).isEqualTo(CondicaoEnum.USADO);
    }

    @Test
    void update_DeveAtualizarProdutoExistente() {
        // Arrange
        Produto produtoAtualizado = criarProduto(1L, "Produto Atualizado","Jonas Oliveira", "ACAO",25, BigDecimal.valueOf(300));

        when(produtoRepository.update(produtoAtualizado)).thenReturn(produtoAtualizado);

        // Act
        Produto resultado = produtoService.update(produtoAtualizado);

        // Assert
        verify(produtoRepository, times(1)).update(produtoAtualizado);
        assertThat(resultado).isEqualTo(produtoAtualizado);
        assertThat(resultado.getNome()).isEqualTo("Produto Atualizado");
        assertThat(resultado.getEstoque()).isEqualTo(25);
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(300));
    }

    @Test
    void update_DeveLancarExcecaoQuandoProdutoNaoExistir() {
        // Arrange
        Produto produtoInexistente = criarProduto(999L, "Produto Inexistente", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(300));

        when(produtoRepository.update(produtoInexistente)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> produtoService.update(produtoInexistente))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Não foi possível encontrar um produto com o id 999")
                .hasMessageContaining("Crie um novo produto");

        verify(produtoRepository, times(1)).update(produtoInexistente);
    }

    @Test
    void update_DeveAtualizarApenasEstoque() {
        // Arrange
        Produto produtoOriginal = criarProduto(1L, "Produto Original", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(100));
        Produto produtoComEstoqueAtualizado = criarProduto(1L, "Produto Original", "Jonas Oliveira", "ACAO", 50, BigDecimal.valueOf(100));

        when(produtoRepository.update(produtoComEstoqueAtualizado)).thenReturn(produtoComEstoqueAtualizado);

        // Act
        Produto resultado = produtoService.update(produtoComEstoqueAtualizado);

        // Assert
        verify(produtoRepository, times(1)).update(produtoComEstoqueAtualizado);
        assertThat(resultado.getEstoque()).isEqualTo(50);
        assertThat(resultado.getNome()).isEqualTo("Produto Original");
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void update_DeveAtualizarPreco() {
        // Arrange
        Produto produtoComPrecoAtualizado = criarProduto(1L, "Produto Teste", "Jonas Oliveira", "ACAO", 10, BigDecimal.valueOf(150));

        when(produtoRepository.update(produtoComPrecoAtualizado)).thenReturn(produtoComPrecoAtualizado);

        // Act
        Produto resultado = produtoService.update(produtoComPrecoAtualizado);

        // Assert
        verify(produtoRepository, times(1)).update(produtoComPrecoAtualizado);
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(150));
    }

    @Test
    void update_DeveAtualizarCondicao() {
        // Arrange
        Funcionario funcionario = criarFuncionario();
        Produto produtoAtualizado = new Produto(
                1L,
                "Produto Teste",
                "Descrição atualizada",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/produto.jpg",
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(80),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO,
                funcionario);

        when(produtoRepository.update(produtoAtualizado)).thenReturn(produtoAtualizado);

        // Act
        Produto resultado = produtoService.update(produtoAtualizado);

        // Assert
        verify(produtoRepository, times(1)).update(produtoAtualizado);
        assertThat(resultado.getCondicao()).isEqualTo(CondicaoEnum.USADO);
    }

    @Test
    void delete_DeveDeletarProduto() {
        // Arrange
        Long idProduto = 1L;
        doNothing().when(produtoRepository).delete(idProduto);

        // Act
        produtoService.delete(idProduto);

        // Assert
        verify(produtoRepository, times(1)).delete(idProduto);
    }

    @Test
    void delete_DevePermitirDeletarProdutoComIdDiferente() {
        // Arrange
        Long idProduto = 999L;
        doNothing().when(produtoRepository).delete(idProduto);

        // Act
        produtoService.delete(idProduto);

        // Assert
        verify(produtoRepository, times(1)).delete(idProduto);
    }

    @Test
    void create_DeveCriarProdutoComEstoqueZero() {
        // Arrange
        Produto produtoSemEstoque = criarProduto(null, "Produto Sem Estoque", "Jonas Oliveira", "ACAO", 0, BigDecimal.valueOf(100));
        Produto produtoCriado = criarProduto(1L, "Produto Sem Estoque", "Jonas Oliveira", "ACAO", 0, BigDecimal.valueOf(100));

        when(produtoRepository.create(produtoSemEstoque)).thenReturn(produtoCriado);

        // Act
        Produto resultado = produtoService.create(produtoSemEstoque);

        // Assert
        verify(produtoRepository, times(1)).create(produtoSemEstoque);
        assertThat(resultado.getEstoque()).isEqualTo(0);
    }

    @Test
    void update_DeveAtualizarMultiplosCampos() {
        // Arrange
        Funcionario funcionario = criarFuncionario();
        Produto produtoAtualizado = new Produto(
                1L,
                "Nome Atualizado",
                "Nova descrição",
                "Jonas Oliveira",
                CategoriaEnum.AVENTURA,
                "http://nova-imagem.com/produto.jpg",
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(80),
                30,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(0.2),
                BigDecimal.valueOf(0.3),
                CondicaoEnum.USADO,
                funcionario);

        when(produtoRepository.update(produtoAtualizado)).thenReturn(produtoAtualizado);

        // Act
        Produto resultado = produtoService.update(produtoAtualizado);

        // Assert
        verify(produtoRepository, times(1)).update(produtoAtualizado);
        assertThat(resultado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descrição");
        assertThat(resultado.getPreco()).isEqualTo(BigDecimal.valueOf(80));
        assertThat(resultado.getEstoque()).isEqualTo(30);
        assertThat(resultado.getCondicao()).isEqualTo(CondicaoEnum.USADO);
    }
}
