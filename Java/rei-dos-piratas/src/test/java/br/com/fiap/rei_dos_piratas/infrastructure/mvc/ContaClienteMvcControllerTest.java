package br.com.fiap.rei_dos_piratas.infrastructure.mvc;

import br.com.fiap.rei_dos_piratas.domain.Enum.*;
import br.com.fiap.rei_dos_piratas.domain.entity.Page;
import br.com.fiap.rei_dos_piratas.domain.exceptions.EstoqueInsuficienteException;
import br.com.fiap.rei_dos_piratas.domain.exceptions.PagamentoRecusadoException;
import br.com.fiap.rei_dos_piratas.interfaces.controller.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoInDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.endereco.EnderecoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.ConsultaFreteDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.frete.consulta.OpcaoFreteOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.*;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.BoletoOutDto;
import br.com.fiap.rei_dos_piratas.interfaces.dto.pagamento.PixQrCodeOutDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Telas do cliente: carrinho, checkout e pagamento. Renderiza os templates reais (erro de Thymeleaf quebra o teste).
 * Permissões da rota ficam no PermissoesIntegrationTest.
 */
@WebMvcTest(ContaClienteMvcController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"ENDERECO_MANAGE", "PEDIDO_CREATE", "PEDIDO_READ"})
class ContaClienteMvcControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CarrinhoController carrinho;
    @MockBean EnderecoController enderecos;
    @MockBean CartaoClienteController cartoes;
    @MockBean FreteController frete;
    @MockBean PedidoController pedidos;

    private final ProdutoOutDto manga = new ProdutoOutDto(7L, "One Piece Vol. 1", "Volume 1", "Eiichiro Oda",
            CategoriaEnum.AVENTURA, "https://example.com/op1.jpg", new BigDecimal("45.90"), new BigDecimal("45.90"),
            10, new BigDecimal("2"), new BigDecimal("13"), new BigDecimal("19"), new BigDecimal("0.2"), CondicaoEnum.NOVO);
    private final EnderecoOutDto casa = new EnderecoOutDto(3L, 45, "01001000", "Praça da Sé", "Sé", 1L, "São Paulo", 1L, "São Paulo", "SP");

    @BeforeEach
    void carrinhoComUmItem() {
        when(carrinho.visualizarCarrinho()).thenReturn(new CarrinhoOutDto(1L, List.of(new ItemProdutoOutDto(manga, 2))));
        when(enderecos.findAll(anyInt(), anyInt())).thenReturn(new Page<>(1, 0, List.of(casa)));
        when(cartoes.listarCartoesDoClienteAtual()).thenReturn(List.of(new CartaoClienteOutDto(5L, "VISA", "1234", LocalDateTime.now())));
    }

    private PedidoOutDto pedido(TipoPagamentoEnum tipo, StatusEnum status, StatusPagamentoEnum statusPagamento) {
        return new PedidoOutDto(10L, LocalDate.now(), null, null, null, new BigDecimal("113.00"), new BigDecimal("21.20"),
                status, List.of(new ItemProdutoOutDto(manga, 2, new BigDecimal("45.90"), 31L)), "Cliente", "Praça da Sé, 45",
                null, null, null, null, null, null, tipo, null, "pay_1", null, statusPagamento);
    }

    // ---- Carrinho ----

    @Test
    void carrinhoDeveMostrarOsItensEOTotal() throws Exception {
        mvc.perform(get("/web/conta/carrinho"))
                .andExpect(status().isOk())
                .andExpect(view().name("conta/carrinho"))
                .andExpect(model().attribute("total", new BigDecimal("91.80")))
                .andExpect(content().string(containsString("One Piece Vol. 1")));
    }

    @Test
    void adicionarAoCarrinhoDeveDelegarERedirecionarParaOCarrinho() throws Exception {
        mvc.perform(post("/web/conta/carrinho/adicionar").param("produtoId", "7").param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/conta/carrinho"));

        verify(carrinho).adicionarProduto(new ItemProdutoInDto(7L, 2));
    }

    @Test
    void adicionarSemEstoqueDeveMostrarOErroNoCarrinho() throws Exception {
        when(carrinho.adicionarProduto(any())).thenThrow(new EstoqueInsuficienteException("Estoque insuficiente!"));

        mvc.perform(post("/web/conta/carrinho/adicionar").param("produtoId", "7").param("quantidade", "50"))
                .andExpect(redirectedUrl("/web/conta/carrinho"))
                .andExpect(flash().attribute("errorMessage", "Estoque insuficiente!"));
    }

    @Test
    void removerDoCarrinhoDeveDelegar() throws Exception {
        mvc.perform(post("/web/conta/carrinho/remover").param("produtoId", "7").param("quantidade", "1"))
                .andExpect(redirectedUrl("/web/conta/carrinho"));

        verify(carrinho).removerProduto(new ItemProdutoInDto(7L, 1));
    }

    // ---- Checkout ----

    @Test
    void checkoutComCarrinhoVazioDeveVoltarAoCarrinho() throws Exception {
        when(carrinho.visualizarCarrinho()).thenReturn(new CarrinhoOutDto(1L, List.of()));

        mvc.perform(get("/web/conta/checkout"))
                .andExpect(redirectedUrl("/web/conta/carrinho"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void checkoutSemEnderecoEscolhidoListaOsEnderecosSemCotarFrete() throws Exception {
        mvc.perform(get("/web/conta/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("conta/checkout"))
                .andExpect(model().attribute("enderecos", List.of(casa)))
                .andExpect(model().attributeDoesNotExist("opcoesFrete"));

        verifyNoInteractions(frete);
    }

    @Test
    void checkoutComEnderecoDeveCotarOFreteDosItensDoCarrinho() throws Exception {
        // Arrange
        when(enderecos.findById(3L)).thenReturn(casa);
        OpcaoFreteOutDto sedex = new OpcaoFreteOutDto(2L, "SEDEX", new BigDecimal("21.20"), new BigDecimal("21.20"),
                BigDecimal.ZERO, "R$", 3, new OpcaoFreteOutDto.Company(1L, "Correios", null));
        when(frete.calcularFreteProdutos(any())).thenReturn(List.of(sedex));

        // Act
        mvc.perform(get("/web/conta/checkout").param("enderecoId", "3"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("opcoesFrete", List.of(sedex)))
                .andExpect(model().attribute("cartoes", hasSize(1)))
                .andExpect(content().string(containsString("SEDEX")));

        // Assert
        ArgumentCaptor<ConsultaFreteDto> captor = ArgumentCaptor.forClass(ConsultaFreteDto.class);
        verify(frete).calcularFreteProdutos(captor.capture());
        assertThat(captor.getValue().cepDestino()).isEqualTo("01001000");
        assertThat(captor.getValue().itens()).containsExactly(new ItemProdutoInDto(7L, 2));
    }

    @Test
    void novoEnderecoDeveSerSalvoEVoltarAoCheckoutComEleEscolhido() throws Exception {
        when(enderecos.save(any())).thenReturn(casa);

        mvc.perform(post("/web/conta/enderecos").param("numero", "45").param("cep", "01001000")
                        .param("logradouro", "Praça da Sé").param("bairro", "Sé").param("cidade", "São Paulo")
                        .param("estadoNome", "São Paulo").param("estadoSigla", "SP"))
                .andExpect(redirectedUrl("/web/conta/checkout?enderecoId=3"));

        verify(enderecos).save(new EnderecoInDto(45, "01001000", "Praça da Sé", "Sé", "São Paulo", "São Paulo", "SP"));
    }

    @Test
    void finalizarComPixDeveCriarOPedidoComOsItensLimparOCarrinhoEIrParaOPagamento() throws Exception {
        // Arrange
        when(pedidos.fazerPedido(any(), any())).thenReturn(pedido(TipoPagamentoEnum.PIX, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.PENDENTE));

        // Act
        mvc.perform(post("/web/conta/checkout").param("enderecoId", "3").param("freteServiceId", "2").param("tipoPagamento", "PIX"))
                .andExpect(redirectedUrl("/web/conta/pedidos/10/pagamento"));

        // Assert
        ArgumentCaptor<PedidoInDto> captor = ArgumentCaptor.forClass(PedidoInDto.class);
        verify(pedidos).fazerPedido(captor.capture(), any());
        assertThat(captor.getValue()).isEqualTo(new PedidoInDto(2L, 3L, List.of(new ItemProdutoInDto(7L, 2)), TipoPagamentoEnum.PIX, null));
        verify(carrinho).limparCarrinho();
    }

    @Test
    void finalizarComCartaoNovoDeveEnviarOCartaoAsParcelasEOIpDoComprador() throws Exception {
        // Arrange
        when(pedidos.fazerPedido(any(), any())).thenReturn(pedido(TipoPagamentoEnum.CREDIT_CARD, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.CONFIRMADO));

        // Act
        mvc.perform(post("/web/conta/checkout").param("enderecoId", "3").param("freteServiceId", "2")
                        .param("tipoPagamento", "CREDIT_CARD").param("titular", "JOAO SILVA").param("numero", "4444444444444444")
                        .param("mesValidade", "12").param("anoValidade", "2030").param("cvv", "123").param("parcelas", "2")
                        .header("X-Forwarded-For", "200.1.2.3, 10.0.0.1"))
                .andExpect(redirectedUrl("/web/conta/pedidos/10/pagamento"));

        // Assert
        ArgumentCaptor<PedidoInDto> captor = ArgumentCaptor.forClass(PedidoInDto.class);
        verify(pedidos).fazerPedido(captor.capture(), eq("200.1.2.3"));
        assertThat(captor.getValue().cartao()).isEqualTo(new CartaoPedidoInDto(null, "JOAO SILVA", "4444444444444444", "12", "2030", "123", 2));
    }

    @Test
    void finalizarComCartaoSalvoDeveEnviarSoOIdDoCartao() throws Exception {
        // Arrange
        when(pedidos.fazerPedido(any(), any())).thenReturn(pedido(TipoPagamentoEnum.CREDIT_CARD, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.CONFIRMADO));

        // Act
        mvc.perform(post("/web/conta/checkout").param("enderecoId", "3").param("freteServiceId", "2")
                        .param("tipoPagamento", "CREDIT_CARD").param("cartaoId", "5").param("parcelas", "1"))
                .andExpect(redirectedUrl("/web/conta/pedidos/10/pagamento"));

        // Assert
        ArgumentCaptor<PedidoInDto> captor = ArgumentCaptor.forClass(PedidoInDto.class);
        verify(pedidos).fazerPedido(captor.capture(), any());
        assertThat(captor.getValue().cartao()).isEqualTo(new CartaoPedidoInDto(5L, null, null, null, null, null, 1));
    }

    @Test
    void cartaoRecusadoDeveVoltarAoCheckoutComAMensagemSemLimparOCarrinhoNemExporOCartao() throws Exception {
        // Arrange
        when(pedidos.fazerPedido(any(), any())).thenThrow(new PagamentoRecusadoException("Pagamento recusado"));

        // Act
        MvcResult resultado = mvc.perform(post("/web/conta/checkout").param("enderecoId", "3").param("freteServiceId", "2")
                        .param("tipoPagamento", "CREDIT_CARD").param("titular", "JOAO SILVA").param("numero", "5184019740373151")
                        .param("mesValidade", "12").param("anoValidade", "2030").param("cvv", "987").param("parcelas", "1"))
                .andExpect(redirectedUrl("/web/conta/checkout?enderecoId=3"))
                .andExpect(flash().attribute("errorMessage", "Pagamento recusado"))
                .andReturn();

        // Assert
        verify(carrinho, never()).limparCarrinho();
        assertThat(resultado.getFlashMap().toString()).doesNotContain("5184019740373151", "987");
    }

    // ---- Pagamento ----

    @Test
    void pagamentoPixDeveMostrarOQrCodeEOCopiaECola() throws Exception {
        // Arrange
        when(pedidos.findById(10L)).thenReturn(pedido(TipoPagamentoEnum.PIX, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.PENDENTE));
        when(pedidos.obterQrCodeCobrancaPix(10L)).thenReturn(new PixQrCodeOutDto(true, "iVBORw0KGgo=", "00020126pix", LocalDateTime.now().plusDays(1), "Pedido 10"));

        // Act & Assert
        mvc.perform(get("/web/conta/pedidos/10/pagamento"))
                .andExpect(status().isOk())
                .andExpect(view().name("conta/pagamento"))
                .andExpect(content().string(containsString("00020126pix")))
                .andExpect(content().string(containsString("data:image/png;base64,iVBORw0KGgo=")));
        verify(pedidos, never()).obterBoleto(any());
    }

    @Test
    void pagamentoBoletoDeveMostrarALinhaDigitavelEOPdf() throws Exception {
        // Arrange
        when(pedidos.findById(10L)).thenReturn(pedido(TipoPagamentoEnum.BOLETO, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.PENDENTE));
        when(pedidos.obterBoleto(10L)).thenReturn(new BoletoOutDto("00190000090123456789", "00191234500000113", "https://asaas/b/pdf/1", LocalDate.of(2026, 11, 7)));

        // Act & Assert
        mvc.perform(get("/web/conta/pedidos/10/pagamento"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("00190000090123456789")))
                .andExpect(content().string(containsString("https://asaas/b/pdf/1")));
        verify(pedidos, never()).obterQrCodeCobrancaPix(any());
    }

    @Test
    void pagamentoCartaoDeveMostrarOStatusSemConsultarPixNemBoleto() throws Exception {
        // Arrange
        when(pedidos.findById(10L)).thenReturn(pedido(TipoPagamentoEnum.CREDIT_CARD, StatusEnum.AGUARDANDO_PAGAMENTO, StatusPagamentoEnum.CONFIRMADO));

        // Act & Assert
        mvc.perform(get("/web/conta/pedidos/10/pagamento"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CONFIRMADO")))
                .andExpect(content().string(not(containsString("data:image/png;base64"))));
        verify(pedidos, never()).obterQrCodeCobrancaPix(any());
        verify(pedidos, never()).obterBoleto(any());
    }

    private static org.hamcrest.Matcher<java.util.Collection<?>> hasSize(int tamanho) {
        return org.hamcrest.Matchers.hasSize(tamanho);
    }
}
