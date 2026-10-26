package br.com.fiap.rei_dos_piratas.interfaces.controller.impl;

import br.com.fiap.rei_dos_piratas.application.service.ClienteService;
import br.com.fiap.rei_dos_piratas.application.service.EnderecoService;
import br.com.fiap.rei_dos_piratas.application.service.PedidoService;
import br.com.fiap.rei_dos_piratas.application.service.ProdutoService;
import br.com.fiap.rei_dos_piratas.application.service.UsuarioAtualService;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoConta;
import br.com.fiap.rei_dos_piratas.domain.Enum.CategoriaEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.CondicaoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.SexoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.interfaces.controller.PedidoController;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.ItemProdutoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
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
    private EnderecoService enderecoService;
    private PedidoController pedidoController;
    private UsuarioAtualService usuarioAtual;

    @BeforeEach
    void setUp() {
        this.pedidoService = mock(PedidoService.class);
        this.clienteService = mock(ClienteService.class);
        this.produtoService = mock(ProdutoService.class);
        this.enderecoService = mock(EnderecoService.class);
        this.usuarioAtual = mock(UsuarioAtualService.class);
        when(usuarioAtual.identidade()).thenReturn(new IdentidadeConta(TipoConta.CLIENTE, 1L));
        this.pedidoController = new PedidoControllerImpl(pedidoService, clienteService, produtoService, enderecoService, usuarioAtual);
    }

    private Cliente criarCliente() {
        Perfil perfilCliente = new Perfil(1L, "CLIENT", "Perfil de cliente", null);
        return new Cliente(
                1L,
                "jonasdasneves",
                "Jonas da Silva Campos Melo",
                "jonas@gmail.com",
                "SenhaSegura123",
                true,
                LocalDate.now(),
                perfilCliente,
                LocalDate.of(2000, 3, 16),
                SexoEnum.M,
                "52998224725",
                "11999999999",
                new Carrinho());
    }

    private Endereco criarEndereco() {
        Estado estado = new Estado(1L, "Sao Paulo", "SP");
        Cidade cidade = new Cidade(1L, "Sao Paulo", estado);
        return new Endereco(1L, 12345, "12345678", "Avenida Paulista", "Bela Vista", true, cidade, "Brasil", "BR", null);
    }

    private Produto criarProduto() {
        Perfil perfilFuncionario = new Perfil(1L, "FUNCIONARIO", "Perfil de funcionário", null);
        Funcionario funcionario = new Funcionario(
                "vendedor01",
                1L,
                "Joao Vendedor",
                "joao@gmail.com",
                "senha123",
                true,
                LocalDate.now(),
                perfilFuncionario,
                null,
                BigDecimal.valueOf(2000));

        return new Produto(
                1L,
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
                funcionario);
    }

    private static final Long FRETE_SERVICE_ID = 3L;

    private Pedido criarPedido(StatusEnum status) {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setDataPedido(LocalDate.now());
        pedido.setStatus(status);
        pedido.setCliente(criarCliente());
        pedido.setEnderecoEntrega(criarEndereco());
        pedido.setValorFrete(BigDecimal.valueOf(21.19));
        pedido.setValorTotal(BigDecimal.valueOf(221.19));
        pedido.setServicoEntrega(3L);
        pedido.setProdutosAdicionados(List.of(new ItemProdutoPedido(criarProduto(), 2)));
        return pedido;
    }

    @Test
    void findAllByCliente() {
        Page<Pedido> page = new Page<>(1, 0, List.of(criarPedido(StatusEnum.AGUARDANDO_PAGAMENTO)));
        when(pedidoService.findAll(0, 10)).thenReturn(page);

        Page<PedidoOutDto> result = pedidoController.findAllByCliente(0, 10);

        verify(pedidoService, times(1)).findAll(0, 10);
        assertThat(result.pageItems()).hasSize(1);
        assertThat(result.pageItems().get(0).status()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void findById() {
        when(pedidoService.findById(1L)).thenReturn(criarPedido(StatusEnum.AGUARDANDO_PAGAMENTO));

        PedidoOutDto result = pedidoController.findById(1L);

        verify(pedidoService, times(1)).findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void fazerPedido() {
        Cliente cliente = criarCliente();
        Endereco endereco = criarEndereco();
        Produto produto = criarProduto();
        Pedido pedidoCriado = criarPedido(StatusEnum.AGUARDANDO_PAGAMENTO);

        PedidoInDto inDto = new PedidoInDto(
                FRETE_SERVICE_ID,
                1L,
                List.of(new ItemProdutoInDto(1L, 2)),
                br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum.PIX
        );

        when(clienteService.findById(1L)).thenReturn(cliente);
        when(enderecoService.findById(1L)).thenReturn(endereco);
        when(produtoService.findById(1L)).thenReturn(produto);
        when(pedidoService.fazerPedido(any(Pedido.class))).thenReturn(pedidoCriado);

        PedidoOutDto result = pedidoController.fazerPedido(inDto, "200.1.2.3");

        verify(clienteService, times(1)).findById(1L);
        verify(enderecoService, times(1)).findById(1L);
        verify(produtoService, times(1)).findById(1L);
        verify(pedidoService, times(1)).fazerPedido(any(Pedido.class));
        assertThat(result.status()).isEqualTo(StatusEnum.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void fazerPedido_CartaoSemDadosDoCartaoDeveLancarRegraDeNegocioSemChamarOServico() {
        // Arrange
        PedidoInDto inDto = new PedidoInDto(
                FRETE_SERVICE_ID,
                1L,
                List.of(new ItemProdutoInDto(1L, 2)),
                br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum.CREDIT_CARD,
                null);
        when(clienteService.findById(1L)).thenReturn(criarCliente());
        when(enderecoService.findById(1L)).thenReturn(criarEndereco());
        when(produtoService.findById(1L)).thenReturn(criarProduto());

        // Act & Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pedidoController.fazerPedido(inDto, "200.1.2.3"))
                .isInstanceOf(br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException.class);
        verifyNoInteractions(pedidoService);
    }

    @Test
    void cancelarPedido() {
        Pedido cancelado = criarPedido(StatusEnum.CANCELADO);
        cancelado.setDataCancelamento(LocalDate.now());
        when(pedidoService.cancelarPedido(1L)).thenReturn(cancelado);

        PedidoOutDto result = pedidoController.cancelarPedido(1L);

        verify(pedidoService, times(1)).cancelarPedido(1L);
        assertThat(result.status()).isEqualTo(StatusEnum.CANCELADO);
    }

    @Test
    void fazerPedido_CartaoDeveRepassarOIpDoCompradorAoServico() {
        // Arrange
        PedidoInDto inDto = new PedidoInDto(
                FRETE_SERVICE_ID,
                1L,
                List.of(new ItemProdutoInDto(1L, 2)),
                br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum.CREDIT_CARD,
                new br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.CartaoPedidoInDto(5L, null, null, null, null, null, 1));
        when(clienteService.findById(1L)).thenReturn(criarCliente());
        when(enderecoService.findById(1L)).thenReturn(criarEndereco());
        when(produtoService.findById(1L)).thenReturn(criarProduto());
        when(pedidoService.fazerPedido(any(Pedido.class), any(PagamentoCartao.class))).thenReturn(criarPedido(StatusEnum.AGUARDANDO_PAGAMENTO));

        // Act
        pedidoController.fazerPedido(inDto, "200.1.2.3");

        // Assert
        org.mockito.ArgumentCaptor<PagamentoCartao> captor = org.mockito.ArgumentCaptor.forClass(PagamentoCartao.class);
        verify(pedidoService).fazerPedido(any(Pedido.class), captor.capture());
        assertThat(captor.getValue().ipRemoto()).isEqualTo("200.1.2.3");
    }
}
