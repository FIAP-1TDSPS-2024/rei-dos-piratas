package br.com.fiap.rei_dos_piratas.application.service.impl;

import br.com.fiap.rei_dos_piratas.domain.entity.*;
import br.com.fiap.rei_dos_piratas.domain.repository.DevolucaoRepository;
import br.com.fiap.rei_dos_piratas.application.service.FreteService;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.jpa.negocio.*;
import br.com.fiap.rei_dos_piratas.infrastructure.mapper.dto.negocio.ItemProdutoDtoMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrecoUnitarioTest {
    @Test
    void dominioExigePrecoObrigatorioNaoNegativoEComDuasCasasDecimais() {
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.TEN);
        var pedido = new ItemProdutoPedido(1L, produto, 1);
        var carrinho = new ItemProdutoCarrinho(1L, produto, 1);
        var devolucao = new ItemDevolucao(null, pedido, 1);
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (var preco : java.util.Arrays.asList(null, new BigDecimal("-1.00"), new BigDecimal("1.001"))) {
                pedido.setPrecoUnitario(preco);
                carrinho.setPrecoUnitario(preco);
                devolucao.setPrecoUnitario(preco);
                for (Object item : List.of(pedido, carrinho, devolucao)) {
                    assertThat(validator.validateProperty(item, "precoUnitario")).isNotEmpty();
                }
            }
            pedido.setPrecoUnitario(new BigDecimal("25.50"));
            assertThat(validator.validateProperty(pedido, "precoUnitario")).isEmpty();
        }
    }

    @Test
    void carrinhoPreservaPrecoNaPersistenciaENaResposta() {
        Produto produto = new Produto();
        produto.setPreco(new BigDecimal("25.50"));
        ItemProdutoCarrinho item = new ItemProdutoCarrinho(1L, produto, 2);
        produto.setPreco(new BigDecimal("99.90"));
        var jpa = JpaItemProdutoMapper.toJpaProdutosCarrinhoEntity(item);
        var recuperado = JpaItemProdutoMapper.toEntity(jpa);
        assertThat(recuperado.getPrecoUnitario()).isEqualByComparingTo("25.50");
        assertThat(ItemProdutoDtoMapper.toDto(recuperado).subtotal()).isEqualByComparingTo("51.00");
    }

    @Test
    void aprovacaoUsaPrecoDaDevolucaoNoTotalEValorDeclarado() {
        Produto produto = new Produto();
        produto.setPreco(new BigDecimal("99.90"));
        produto.setPeso(BigDecimal.ONE);
        produto.setProfundidade(BigDecimal.ONE);
        ItemProdutoPedido comprado = new ItemProdutoPedido(1L, produto, 3, new BigDecimal("25.50"));
        Devolucao devolucao = new Devolucao();
        devolucao.setId(1L);
        devolucao.setPedido(mock(Pedido.class, RETURNS_DEEP_STUBS));
        devolucao.setItens(List.of(new ItemDevolucao(null, comprado, 2)));
        devolucao.setValorTotal(new BigDecimal("51.00"));
        devolucao.setServicoEntrega(1L);
        var repository = mock(DevolucaoRepository.class);
        var frete = mock(FreteService.class);
        when(repository.findById(1L)).thenReturn(devolucao);
        when(repository.update(devolucao)).thenReturn(devolucao);
        when(frete.criarPedidoDevolucaoFrete(any())).thenReturn(
                new br.com.fiap.rei_dos_piratas.interfaces.dto.frete.devolucao.DevolucaoFreteResponseDto(
                        java.util.UUID.randomUUID(), "protocolo", 1, null, BigDecimal.TEN,
                        null, null, null, null, null, null, 1));
        new DevolucaoServiceImpl(repository, frete).aprovarDevolucao(1L);
        assertThat(devolucao.getValorTotal()).isEqualByComparingTo("61.00");
        verify(frete).criarPedidoDevolucaoFrete(argThat(dto -> dto.insurance_value().compareTo(new BigDecimal("51.00")) == 0));
    }

    @Test
    void persistenciaPreservaPrecoMesmoComAlteracaoDoCatalogo() {
        Produto produto = mock(Produto.class);
        when(produto.getPreco()).thenReturn(new BigDecimal("25.50"));
        ItemProdutoPedido item = new ItemProdutoPedido(1L, produto, 3);
        when(produto.getPreco()).thenReturn(new BigDecimal("99.90"));
        var jpa = JpaItemProdutoMapper.toJpaProdutosPedidosEntity(item);
        assertThat(jpa.getPrecoUnitario()).isEqualByComparingTo("25.50");
        assertThat(JpaItemProdutoMapper.toEntity(jpa).getPrecoUnitario()).isEqualByComparingTo("25.50");
        var dto = ItemProdutoDtoMapper.toDto(item);
        assertThat(dto.precoUnitario()).isEqualByComparingTo("25.50");
        assertThat(dto.subtotal()).isEqualByComparingTo("76.50");
        ItemDevolucao devolvido = new ItemDevolucao(null, item, 2);
        var jpaDevolucao = JpaItemDevolucaoMapper.toJpaEntity(devolvido);
        assertThat(jpaDevolucao.getPrecoUnitario()).isEqualByComparingTo("25.50");
    }

    @Test
    void solicitacaoCalculaReembolsoParcialPeloPrecoPago() {
        Produto produto = mock(Produto.class);
        when(produto.getPreco()).thenReturn(new BigDecimal("100.00"));
        ItemProdutoPedido comprado = new ItemProdutoPedido(1L, produto, 3, new BigDecimal("25.50"));
        ItemDevolucao item = new ItemDevolucao(null, comprado, 2);
        Pedido pedido = mock(Pedido.class);
        when(pedido.getId()).thenReturn(1L);
        MotivoDevolucao motivo = mock(MotivoDevolucao.class);
        when(motivo.getArrependimento()).thenReturn(false);
        Devolucao devolucao = new Devolucao();
        devolucao.setPedido(pedido);
        devolucao.setMotivo(motivo);
        devolucao.setItens(List.of(item));
        DevolucaoRepository repository = mock(DevolucaoRepository.class);
        when(repository.findAllByPedidoId(1L)).thenReturn(List.of());
        when(repository.create(devolucao)).thenReturn(devolucao);
        new DevolucaoServiceImpl(repository, mock(FreteService.class)).solicitarDevolucao(devolucao);
        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("25.50");
        assertThat(devolucao.getValorTotal()).isEqualByComparingTo("51.00");
    }
}
