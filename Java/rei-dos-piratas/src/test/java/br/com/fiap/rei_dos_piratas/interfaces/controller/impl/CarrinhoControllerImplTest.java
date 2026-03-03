package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.CarrinhoService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.Role;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.interfaces.controller.CarrinhoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteCompanyDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.FreteServiceDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoCarrinhoInDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CarrinhoControllerImplTest {

    private CarrinhoService carrinhoService;
    private ProdutoService produtoService;
    private CarrinhoController carrinhoController;
    private EnderecoService enderecoService;

    @BeforeEach
    void setUp() {
        this.carrinhoService = mock(CarrinhoService.class);
        this.produtoService = mock(ProdutoService.class);
        this.enderecoService = mock(EnderecoService.class);
        this.carrinhoController = new CarrinhoControllerImpl(carrinhoService, produtoService, enderecoService);
    }

    private Produto criarProduto() {
        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "Joao Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                Role.USER,
                null,
                BigDecimal.valueOf(2000));

        return new Produto(
                1L,
                "Action Figure One Piece",
                "Action figure do Luffy em alta qualidade",
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
                funcionario);
    }

    private Endereco criarEndereco() {
        Estado estado = new Estado(1L, "Sao Paulo", "SP");
        Cidade cidade = new Cidade(1L, "Sao Paulo", estado);
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
                null);
    }

    private FreteServiceDto criarFrete() {
        FreteCompanyDto company = new FreteCompanyDto(2L, "Jadlog", "https://sandbox.melhorenvio.com.br/images/shipping-companies/jadlog.png");
        return new FreteServiceDto(3L, ".Package", BigDecimal.valueOf(21.19), BigDecimal.valueOf(21.19), BigDecimal.ZERO, "R$", 6, company);
    }

    @Test
    void adicionarProduto() {
        Long produtoId = 1L;
        int quantidade = 2;

        Produto produto = criarProduto();
        ItemProdutoCarrinho item = new ItemProdutoCarrinho(produto, quantidade);
        Carrinho carrinho = new Carrinho(1L, List.of(item));

        when(produtoService.findById(produtoId)).thenReturn(produto);
        when(carrinhoService.adicionarProduto(any(ItemProdutoPedido.class))).thenReturn(carrinho);

        carrinhoController.adicionarProduto(new ItemProdutoInDto(produtoId, quantidade));

        verify(produtoService, times(1)).findById(produtoId);
        verify(carrinhoService, times(1)).adicionarProduto(any(ItemProdutoPedido.class));
    }

    @Test
    void removerProduto() {
        Long produtoId = 1L;
        int quantidade = 1;

        Produto produto = criarProduto();
        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());

        when(produtoService.findById(produtoId)).thenReturn(produto);
        when(carrinhoService.removerProduto(any(ItemProdutoPedido.class))).thenReturn(carrinho);

        carrinhoController.removerProduto(new ItemProdutoInDto(produtoId, quantidade));

        verify(produtoService, times(1)).findById(produtoId);
        verify(carrinhoService, times(1)).removerProduto(any(ItemProdutoPedido.class));
    }

    @Test
    void limparCarrinho() {
        Carrinho carrinho = new Carrinho(1L, new ArrayList<>());
        when(carrinhoService.limparCarrinho()).thenReturn(carrinho);

        carrinhoController.limparCarrinho();

        verify(carrinhoService, times(1)).limparCarrinho();
    }

    @Test
    void visualizarCarrinho() {
        Produto produto = criarProduto();
        ItemProdutoCarrinho item = new ItemProdutoCarrinho(produto, 2);
        Carrinho carrinho = new Carrinho(1L, List.of(item));

        when(carrinhoService.visualizarCarrinho()).thenReturn(carrinho);

        carrinhoController.visualizarCarrinho();

        verify(carrinhoService, times(1)).visualizarCarrinho();
    }

    @Test
    void finalizarCompra() {
        Endereco endereco = criarEndereco();
        FreteServiceDto frete = criarFrete();
        PedidoCarrinhoInDto pedidoIn = new PedidoCarrinhoInDto(frete, 1L);

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
                "52998224725",
                "11999999999",
                new Carrinho());

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setEnderecoEntrega(endereco);
        pedido.setProdutosAdicionados(List.of(new ItemProdutoPedido(criarProduto(), 2)));
        pedido.setStatus(StatusEnum.AGUARDANDO_PAGAMENTO);
        pedido.setDataPedido(LocalDate.now());
        pedido.setValorFrete(BigDecimal.valueOf(21.19));
        pedido.setValorTotal(BigDecimal.valueOf(221.19));

        when(enderecoService.findById(1L)).thenReturn(endereco);
        when(carrinhoService.finalizarCompra(endereco, frete)).thenReturn(pedido);

        carrinhoController.finalizarCompra(pedidoIn);

        verify(enderecoService, times(1)).findById(1L);
        verify(carrinhoService, times(1)).finalizarCompra(endereco, frete);
    }
}
