package br.com.fiap.rei_dos_piratas.domain.entity;

import br.com.fiap.rei_dos_piratas.domain.Enum.OrigemEstornoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Valor reembolsado na devolução: itens pelo preço pago; frete de ida só no arrependimento do pedido inteiro. */
class DevolucaoReembolsoTest {

    private final ItemProdutoPedido manga = item(1L, "45.90", 2);
    private final ItemProdutoPedido box = item(2L, "120.00", 1);

    private static ItemProdutoPedido item(Long id, String preco, int quantidade) {
        Produto produto = new Produto();
        produto.setPreco(new BigDecimal("999.00")); // preço atual do catálogo, diferente do pago
        return new ItemProdutoPedido(id, produto, quantidade, new BigDecimal(preco));
    }

    private Devolucao devolucao(boolean arrependimento, List<ItemDevolucao> itens) {
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setProdutosAdicionados(List.of(manga, box));
        pedido.setValorFrete(new BigDecimal("21.19"));
        MotivoDevolucao motivo = new MotivoDevolucao();
        motivo.setArrependimento(arrependimento);
        Devolucao devolucao = new Devolucao();
        devolucao.setId(7L);
        devolucao.setPedido(pedido);
        devolucao.setMotivo(motivo);
        devolucao.setItens(itens);
        devolucao.setValorFrete(new BigDecimal("15.00")); // frete de volta, pago pela loja
        return devolucao;
    }

    @Test
    void devolucaoParcialReembolsaSoOsItensPeloPrecoPago() {
        Devolucao devolucao = devolucao(false, List.of(new ItemDevolucao(null, manga, 1)));

        assertThat(devolucao.valorAReembolsar()).isEqualByComparingTo("45.90");
    }

    @Test
    void freteDeVoltaNuncaEntraNoReembolso() {
        Devolucao devolucao = devolucao(false, List.of(new ItemDevolucao(null, manga, 2), new ItemDevolucao(null, box, 1)));

        // 2 x 45,90 + 120,00, sem os 15,00 do frete de volta
        assertThat(devolucao.valorAReembolsar()).isEqualByComparingTo("211.80");
    }

    @Test
    void arrependimentoDoPedidoInteiroReembolsaTambemOFreteDeIda() {
        Devolucao devolucao = devolucao(true, List.of(new ItemDevolucao(null, manga, 2), new ItemDevolucao(null, box, 1)));

        assertThat(devolucao.valorAReembolsar()).isEqualByComparingTo("232.99");
    }

    @Test
    void arrependimentoParcialNaoReembolsaOFreteDeIda() {
        Devolucao devolucao = devolucao(true, List.of(new ItemDevolucao(null, manga, 2)));

        assertThat(devolucao.valorAReembolsar()).isEqualByComparingTo("91.80");
    }

    @Test
    void arrependimentoComQuantidadeMenorQueACompradaNaoReembolsaOFrete() {
        Devolucao devolucao = devolucao(true, List.of(new ItemDevolucao(null, manga, 1), new ItemDevolucao(null, box, 1)));

        assertThat(devolucao.valorAReembolsar()).isEqualByComparingTo("165.90");
    }

    @Test
    void mesmaDevolucaoNaoPodeSerEstornadaDuasVezes() {
        // Arrange
        Pagamento pagamento = Pagamento.pendente(TipoPagamentoEnum.PIX);
        pagamento.setValor(new BigDecimal("232.99"));
        pagamento.setStatus(StatusPagamentoEnum.RECEBIDO);
        pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 7L, new BigDecimal("45.90"));

        // Act & Assert
        assertThatThrownBy(() -> pagamento.solicitarEstorno(OrigemEstornoEnum.DEVOLUCAO, 7L, new BigDecimal("45.90")))
                .isInstanceOf(RegraDeNegocioException.class);
    }
}
