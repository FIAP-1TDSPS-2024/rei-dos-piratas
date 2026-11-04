package br.com.fiap.rei_dos_piratas.interfaces.mapper.negocio;

import br.com.fiap.rei_dos_piratas.domain.Enum.TipoPagamentoEnum;
import br.com.fiap.rei_dos_piratas.domain.entity.Pagamento;
import br.com.fiap.rei_dos_piratas.domain.entity.Pedido;
import br.com.fiap.rei_dos_piratas.interfaces.dto.negocio.PedidoOutDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoDtoMapperTest {

    @Test
    void camposDePagamentoDaRespostaVemDoPagamentoDoPedido() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L, TipoPagamentoEnum.PIX);
        pedido.getPagamento().setIdCobranca("pay_dto");
        pedido.getPagamento().setValorLiquido(new BigDecimal("219.20"));

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.tipoPagamento()).isEqualTo(TipoPagamentoEnum.PIX);
        assertThat(dto.idCobranca()).isEqualTo("pay_dto");
        assertThat(dto.valorLiquido()).isEqualByComparingTo("219.20");
    }

    @Test
    void urlDoBoletoDaRespostaVemDoPagamento() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L, TipoPagamentoEnum.BOLETO);
        pedido.getPagamento().setUrlBoleto("https://sandbox.asaas.com/b/pdf/pay_boleto");

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.urlBoleto()).isEqualTo("https://sandbox.asaas.com/b/pdf/pay_boleto");
    }

    @Test
    void pedidoSemPagamentoRetornaCamposDePagamentoNulos() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L);

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.tipoPagamento()).isNull();
        assertThat(dto.idCobranca()).isNull();
        assertThat(dto.valorLiquido()).isNull();
    }

    @Test
    void statusDoPagamentoDaRespostaVemDoPagamento() {
        // Arrange
        Pedido pedido = new Pedido(null, null, new ArrayList<>(), 1L, TipoPagamentoEnum.CREDIT_CARD);
        pedido.getPagamento().setStatus(br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum.CONFIRMADO);

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.statusPagamento()).isEqualTo(br.com.fiap.rei_dos_piratas.domain.Enum.StatusPagamentoEnum.CONFIRMADO);
    }

    @Test
    void itensDoPedidoDevemTrazerOIdDoItemParaPedirDevolucao() {
        // Arrange
        br.com.fiap.rei_dos_piratas.domain.entity.Produto produto = new br.com.fiap.rei_dos_piratas.domain.entity.Produto();
        produto.setId(9L);
        produto.setPreco(new BigDecimal("45.90"));
        Pedido pedido = new Pedido(null, null, new ArrayList<>(java.util.List.of(
                new br.com.fiap.rei_dos_piratas.domain.entity.ItemProdutoPedido(31L, produto, 2))), 1L, TipoPagamentoEnum.PIX);

        // Act
        PedidoOutDto dto = PedidoDtoMapper.toDto(pedido);

        // Assert
        assertThat(dto.produtosAdicionados()).singleElement().satisfies(item -> assertThat(item.itemPedidoId()).isEqualTo(31L));
    }
}
