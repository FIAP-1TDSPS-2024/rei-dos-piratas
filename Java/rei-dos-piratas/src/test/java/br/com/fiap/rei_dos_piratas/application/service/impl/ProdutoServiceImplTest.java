package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ResourceNotFoundException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.ArmazenamentoArquivoException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import br.com.fiap.rei_dos_piratas.domain.gateway.GatewayArquivo;
import br.com.fiap.rei_dos_piratas.domain.repository.ProdutoRepository;
import br.com.fiap.rei_dos_piratas.application.service.UrlUploadGerada;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProdutoServiceImplTest {

    private static final String CHAVE_IMAGEM = "produtos/3f2b9c1e-8d4a-4b6f-9e2a-1c5d7e9f0a1b/imagem.jpg";

    private ProdutoService produtoService;
    private ProdutoRepository produtoRepository;
    private Validator validator;
    private GatewayArquivo gatewayArquivo;

    @BeforeEach
    void setUp() {
        this.produtoRepository = mock(ProdutoRepository.class);
        this.gatewayArquivo = mock(GatewayArquivo.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        // Imagem já enviada ao S3, salvo quando o teste diz o contrário
        when(gatewayArquivo.existe(anyString())).thenReturn(true);
        this.produtoService = new ProdutoServiceImpl(produtoRepository, validator, gatewayArquivo);
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
                CHAVE_IMAGEM,
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
    void gerarUrlPreAssinadaUpload_DeveRetornarKeyEUrlParaEnvioDiretoAoS3() throws Exception {
        URL urlAssinada = new URL("https://exemplo.com/capa.png?assinatura=temporaria");
        when(gatewayArquivo.gerarUrlPreAssinadaUpload(anyString(), anyString(), eq(Duration.ofMinutes(5))))
                .thenReturn(urlAssinada);

        UrlUploadGerada resultado = produtoService.gerarUrlPreAssinadaUpload("image/png");

        assertThat(resultado.url()).isEqualTo(urlAssinada.toString());
        assertThat(resultado.key()).startsWith("produtos/").endsWith(".png");
        verify(gatewayArquivo).gerarUrlPreAssinadaUpload(eq(resultado.key()), eq("image/png"), eq(Duration.ofMinutes(5)));
    }

    @Test
    void gerarUrlPreAssinadaVisualizacao_DeveAssinarKeyInformada() throws Exception {
        URL urlAssinada = new URL("https://exemplo.com/capa.jpg?assinatura=temporaria");
        when(gatewayArquivo.gerarUrlPreAssinadaVisualizacao(CHAVE_IMAGEM, Duration.ofHours(2)))
                .thenReturn(urlAssinada);

        String resultado = produtoService.gerarUrlPreAssinadaVisualizacao(CHAVE_IMAGEM);

        assertThat(resultado).isEqualTo(urlAssinada.toString());
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
                CHAVE_IMAGEM,
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
                CHAVE_IMAGEM,
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
                CHAVE_IMAGEM,
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
                CHAVE_IMAGEM,
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

    // ---- Imagem do produto (S3) ----

    @Test
    void gerarUrlPreAssinadaUpload_UsaAExtensaoEOContentTypeDoArquivo() throws Exception {
        when(gatewayArquivo.gerarUrlPreAssinadaUpload(anyString(), anyString(), any()))
                .thenReturn(new URL("https://exemplo.com/up?assinatura=1"));

        UrlUploadGerada resultado = produtoService.gerarUrlPreAssinadaUpload("image/jpeg");

        assertThat(resultado.key()).matches("produtos/[0-9a-f-]{36}/imagem[.]jpg");
        verify(gatewayArquivo).gerarUrlPreAssinadaUpload(resultado.key(), "image/jpeg", Duration.ofMinutes(5));
    }

    @Test
    void gerarUrlPreAssinadaUpload_RecusaFormatoNaoSuportadoSemChamarOS3() {
        assertThatThrownBy(() -> produtoService.gerarUrlPreAssinadaUpload("application/pdf"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("PNG, JPEG ou WEBP");
        verify(gatewayArquivo, never()).gerarUrlPreAssinadaUpload(any(), any(), any());
    }

    @Test
    void gerarUrlPreAssinadaVisualizacao_RecusaKeyForaDasImagensDeProduto() {
        assertThatThrownBy(() -> produtoService.gerarUrlPreAssinadaVisualizacao("financeiro/relatorio.pdf"))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> produtoService.gerarUrlPreAssinadaVisualizacao(null))
                .isInstanceOf(RegraDeNegocioException.class);
        verify(gatewayArquivo, never()).gerarUrlPreAssinadaVisualizacao(any(), any());
    }

    @Test
    void urlDaImagem_AssinaAChaveDoProduto() throws Exception {
        when(gatewayArquivo.gerarUrlPreAssinadaVisualizacao(CHAVE_IMAGEM, Duration.ofHours(2)))
                .thenReturn(new URL("https://exemplo.com/img?assinatura=2"));

        assertThat(produtoService.urlDaImagem(CHAVE_IMAGEM)).isEqualTo("https://exemplo.com/img?assinatura=2");
    }

    @Test
    void urlDaImagem_DevolveAUrlAntigaComoEstaSemChamarOS3() {
        assertThat(produtoService.urlDaImagem("https://cf.shopee.com.br/file/abc.jpg"))
                .isEqualTo("https://cf.shopee.com.br/file/abc.jpg");
        verify(gatewayArquivo, never()).gerarUrlPreAssinadaVisualizacao(any(), any());
    }

    @Test
    void urlDaImagem_SemImagemOuComValorInvalidoDevolveNulo() {
        assertThat(produtoService.urlDaImagem(null)).isNull();
        assertThat(produtoService.urlDaImagem("")).isNull();
        assertThat(produtoService.urlDaImagem("javascript:alert(1)")).isNull();
    }

    @Test
    void urlDaImagem_FalhaDoS3NaoDerrubaALeituraDoProduto() {
        when(gatewayArquivo.gerarUrlPreAssinadaVisualizacao(any(), any()))
                .thenThrow(new ArmazenamentoArquivoException("indisponível"));

        assertThat(produtoService.urlDaImagem(CHAVE_IMAGEM)).isNull();
    }

    @Test
    void create_RecusaImagemQueAindaNaoFoiEnviada() {
        when(gatewayArquivo.existe(CHAVE_IMAGEM)).thenReturn(false);
        Produto novo = criarProduto(null, "Produto Sem Upload Feito", "Autor", "ACAO", 1, BigDecimal.TEN);

        assertThatThrownBy(() -> produtoService.create(novo))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("imagem");
        verify(produtoRepository, never()).create(any());
    }

    @Test
    void create_RecusaUrlNoLugarDaChaveDaImagem() {
        Produto novo = criarProduto(null, "Produto Com Url Inteira", "Autor", "ACAO", 1, BigDecimal.TEN);
        novo.setEnderecoImagem("https://bucket.s3.amazonaws.com/produtos/x/imagem.png?X-Amz-Signature=abc");

        assertThatThrownBy(() -> produtoService.create(novo)).isInstanceOf(RegraDeNegocioException.class);
        verify(produtoRepository, never()).create(any());
    }

    @Test
    void update_MantendoAImagemAntigaNaoExigeNovoEnvio() {
        Produto atual = criarProduto(1L, "Legado", "Autor", "ACAO", 1, BigDecimal.TEN);
        atual.setEnderecoImagem("https://cf.shopee.com.br/file/abc.jpg");
        Produto editado = criarProduto(1L, "Legado Editado", "Autor", "ACAO", 2, BigDecimal.TEN);
        editado.setEnderecoImagem("https://cf.shopee.com.br/file/abc.jpg");
        when(produtoRepository.findById(1L)).thenReturn(atual);
        when(produtoRepository.update(editado)).thenReturn(editado);

        assertThat(produtoService.update(editado).getNome()).isEqualTo("Legado Editado");
        verify(gatewayArquivo, never()).existe(any());
    }

    @Test
    void update_TrocandoAImagemExigeQueANovaTenhaSidoEnviada() {
        Produto atual = criarProduto(1L, "Produto Com Imagem Nova", "Autor", "ACAO", 1, BigDecimal.TEN);
        atual.setEnderecoImagem("https://cf.shopee.com.br/file/abc.jpg");
        Produto editado = criarProduto(1L, "Produto Com Imagem Nova", "Autor", "ACAO", 1, BigDecimal.TEN);
        when(produtoRepository.findById(1L)).thenReturn(atual);
        when(gatewayArquivo.existe(CHAVE_IMAGEM)).thenReturn(false);

        assertThatThrownBy(() -> produtoService.update(editado)).isInstanceOf(RegraDeNegocioException.class);
        verify(produtoRepository, never()).update(any());
    }
}
