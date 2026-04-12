package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.FuncionarioService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Funcionario;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.entity.Perfil;
import br.com.fiap.rei_dos_piratas.domain.entity.Produto;
import br.com.fiap.rei_dos_piratas.interfaces.controller.ProdutoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ProdutoOutDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private Funcionario criarFuncionario() {
        Perfil perfilFuncionario = new Perfil(1L, "FUNCIONARIO", "Perfil de funcionário", null);
        return new Funcionario(
                "vendedor01",
                1L,
                "Joao Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                perfilFuncionario,
                null,
                BigDecimal.valueOf(1000));
    }

    private Produto criarProduto(Long id) {
        return new Produto(
                id,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incriveis",
                "Eiichiro Oda",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(120),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                CondicaoEnum.NOVO,
                criarFuncionario());
    }

    @Test
    void findById() {
        Produto produto = criarProduto(1L);
        when(produtoService.findById(1L)).thenReturn(produto);

        ProdutoOutDto dto = produtoController.findById(1L);

        verify(produtoService, times(1)).findById(1L);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.autor()).isEqualTo("Eiichiro Oda");
        assertThat(dto.categoria()).isEqualTo(CategoriaEnum.AVENTURA);
    }

    @Test
    void findAll() {
        Page<Produto> page = new Page<>(1, 0, List.of(criarProduto(1L), criarProduto(2L)));
        when(produtoService.findAll(0, 10)).thenReturn(page);

        Page<ProdutoOutDto> result = produtoController.findAll(0, 10);

        verify(produtoService, times(1)).findAll(0, 10);
        assertThat(result.pageItems()).hasSize(2);
        assertThat(result.pageItems().get(0).id()).isEqualTo(1L);
    }

    @Test
    void create() {
        ProdutoInDto dtoIn = new ProdutoInDto(
                null,
                "Action Figure One Piece Luffy Gear 5",
                "Action figure do Luffy em alta qualidade com detalhes incriveis",
                "Eiichiro Oda",
                CategoriaEnum.AVENTURA,
                "http://imagem.com/luffy.jpg",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(120),
                3,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                CondicaoEnum.NOVO,
                1L);

        Funcionario funcionario = criarFuncionario();
        Produto produtoCriado = criarProduto(1L);

        when(funcionarioService.findById(1L)).thenReturn(funcionario);
        when(produtoService.create(any(Produto.class))).thenReturn(produtoCriado);

        ProdutoOutDto result = produtoController.create(dtoIn);

        verify(funcionarioService, times(1)).findById(1L);
        verify(produtoService, times(1)).create(any(Produto.class));
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void update() {
        Produto produtoNovo = criarProduto(1L);
        when(produtoService.update(any(Produto.class))).thenReturn(produtoNovo);

        ProdutoOutDto result = produtoController.update(produtoNovo);

        verify(produtoService, times(1)).update(any(Produto.class));
        assertThat(result.nome()).contains("Luffy Gear 5");
    }

    @Test
    void delete() {
        produtoController.delete(1L);
        verify(produtoService, times(1)).delete(1L);
    }
}
